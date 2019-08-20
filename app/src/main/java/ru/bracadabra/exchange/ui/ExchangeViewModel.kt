package ru.bracadabra.exchange.ui

import android.os.Bundle
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.exceptions.Exceptions
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import ru.bracadabra.exchange.R
import ru.bracadabra.exchange.data.CurrenciesFlagsMapper
import ru.bracadabra.exchange.data.preference.Preferences
import ru.bracadabra.exchange.data.service.ExchangerService
import ru.bracadabra.exchange.data.service.Rate
import ru.bracadabra.exchange.utils.Separator
import ru.bracadabra.exchange.utils.extensions.mapNotNull
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExchangeViewModel(
        private val exchangerService: ExchangerService,
        private val ioScheduler: Scheduler,
        private val computationScheduler: Scheduler,
        private val mainScheduler: Scheduler,
        private val flagsMapper: CurrenciesFlagsMapper,
        private val preferences: Preferences
) : ViewModel() {

    private val currencyValueUpdatesSubject = PublishSubject.create<CharSequence>()
    private val currencyValueUpdates: Observable<CharSequence> = currencyValueUpdatesSubject

    private val baseCurrencyUpdatesSubject = PublishSubject.create<String>()
    private val baseCurrencyUpdates: Observable<String> = baseCurrencyUpdatesSubject

    private val retryRequestSubject = PublishSubject.create<Unit>()
    private val retryRequest: Observable<Unit> = retryRequestSubject

    @Volatile
    private var valuesHolder: ValuesHolder? = null

    fun viewStates(): Observable<ExchangeViewState> {
        return initialRates(preferences.baseCurrency())
                .doOnSuccess { valuesHolder = it }
                .subscribeOn(ioScheduler)
                .flatMapObservable { initialHolder ->
                    baseCurrencyUpdates.scan(initialHolder) { holder, base ->
                        holder.pinBaseCurrency(base)
                    }.switchMap { holder ->
                        Observables.combineLatest(
                                ratesUpdates(holder.base())
                                        .startWith(holder.rates()),
                                currencyValueUpdates()
                                        .startWith(holder.values().first().value.toOptional())
                        ) { rates, (baseValue) ->
                            holder.updateValuesWith(rates, baseValue)
                        }
                                .map { it.toList() }
                                .distinctUntilChanged()
                                .map<ExchangeViewState> { ExchangeViewState.Ready(it) }
                    }
                }
                .observeOn(mainScheduler)
                .startWith(ExchangeViewState.Progress)
                .doOnError { Timber.d(it) }
                .onErrorReturnItem(ExchangeViewState.Error(R.string.exchange_common_error))
                .repeatWhen { it.switchMap { retryRequest } }
    }

    private fun initialRates(baseCurrency: String): Single<ValuesHolder> {
        return if (valuesHolder == null) {
            exchangerService.exchangeRates(baseCurrency)
                    .map { rates ->
                        val values = rates.rates
                                .map { rate -> rate.toExchangeValues(null) }
                                .toMutableList()
                                .addBaseCurrency(rates.base, null)
                        val currenciesToRates = rates.rates.associateByTo(HashMap()) {
                            it.currency
                        }

                        ValuesHolder(values, rates.base, currenciesToRates)
                    }
        } else {
            Single.just(valuesHolder)
        }
    }

    private fun ratesUpdates(base: String): Observable<Map<String, Rate>> {
        return exchangerService.exchangeRates(base)
                .delay(preferences.updateTime(), TimeUnit.SECONDS, computationScheduler)
                .repeatWhen {
                    it.delay(
                            preferences.updateTime(),
                            TimeUnit.SECONDS,
                            computationScheduler
                    )
                }
                .retryWhen { errors ->
                    errors.switchMap { error ->
                        when (error) {
                            is IOException -> Flowable.timer(
                                    preferences.updateTime(),
                                    TimeUnit.SECONDS,
                                    computationScheduler
                            )
                            else -> throw Exceptions.propagate(error)
                        }
                    }
                }
                .map { rates -> rates.rates.associateBy { it.currency } }
                .subscribeOn(ioScheduler)
                .toObservable()
    }

    private fun currencyValueUpdates(): Observable<Optional<Float>> {
        return currencyValueUpdates.mapNotNull { input ->
            when {
                input.isEmpty() -> None
                else -> input.toString()
                        .replace(Separator.EU, Separator.US)
                        .toFloatOrNull()
                        .toOptional()
            }
        }
    }

    private fun Rate.toExchangeValues(value: Float?): ExchangeValue {
        return ExchangeValue(currency, value, flagsMapper.mapFlag(currency))
    }

    private fun MutableList<ExchangeValue>.addBaseCurrency(
            base: String,
            value: Float?
    ): MutableList<ExchangeValue> {
        add(0, ExchangeValue(base, value, flagsMapper.mapFlag(base)))
        return this
    }

    fun updateCurrencyValue(value: CharSequence) {
        currencyValueUpdatesSubject.onNext(value)
    }

    fun updateBaseCurrency(value: String) {
        baseCurrencyUpdatesSubject.onNext(value)
    }

    fun retryRequest() {
        retryRequestSubject.onNext(Unit)
    }

    fun saveState(outState: Bundle) {
        valuesHolder?.let { outState.putParcelable(KEY_SAVED_STATE, it) }
    }

    fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { valuesHolder = it.getParcelable(KEY_SAVED_STATE) }
    }

    /**
     * This is our database. Just for sake of simplicity.
     * */
    @Parcelize
    private data class ValuesHolder(
            private val values: MutableList<ExchangeValue>,
            private var base: String,
            private val rates: HashMap<String, Rate>
    ) : Parcelable {

        fun base(): String {
            return synchronized(this) { base }
        }

        fun values(): List<ExchangeValue> {
            return synchronized(this) { values.toList() }
        }

        fun rates(): Map<String, Rate> {
            return synchronized(this) { rates.toMap() }
        }

        fun pinBaseCurrency(base: String): ValuesHolder {
            return synchronized(this) {
                values.find { it.currency == base }?.let {
                    values.remove(it)
                    values.add(0, it)
                    this.base = it.currency
                }

                this
            }
        }

        fun updateValuesWith(
                newRates: Map<String, Rate>,
                baseValue: Float?
        ): List<ExchangeValue> {
            return synchronized(this) {
                values.forEachIndexed { index, value ->
                    val newValue = when {
                        baseValue == null -> null
                        index == 0 -> baseValue
                        else -> (newRates[value.currency]?.rate ?: 1f) * baseValue
                    }
                    values[index] = value.copy(value = newValue)
                }

                rates.clear()
                rates.putAll(newRates)

                values
            }
        }
    }

    companion object {
        private const val KEY_SAVED_STATE = "key_saved_state"
    }

}