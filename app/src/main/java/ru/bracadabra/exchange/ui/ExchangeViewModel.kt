package ru.bracadabra.exchange.ui

import androidx.lifecycle.ViewModel
import com.gojuno.koptional.None
import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.exceptions.Exceptions
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import ru.bracadabra.exchange.data.CurrenciesFlagsMapper
import ru.bracadabra.exchange.data.preference.Preferences
import ru.bracadabra.exchange.data.service.ExchangerService
import ru.bracadabra.exchange.data.service.Rate
import ru.bracadabra.exchange.utils.Separator
import ru.bracadabra.exchange.utils.extensions.mapNotNull
import java.io.IOException
import java.util.concurrent.TimeUnit

class ExchangeViewModel(
        private val exchangerService: ExchangerService,
        private val ioScheduler: Scheduler,
        private val mainScheduler: Scheduler,
        private val flagsMapper: CurrenciesFlagsMapper,
        private val preferences: Preferences
) : ViewModel() {

    private val currencyValueUpdatesSubject = PublishSubject.create<CharSequence>()
    private val currencyValueUpdates: Observable<CharSequence> = currencyValueUpdatesSubject

    private val baseCurrencyUpdatesSubject = PublishSubject.create<String>()
    private val baseCurrencyUpdates: Observable<String> = baseCurrencyUpdatesSubject

    fun exchangeRates(): Observable<out List<ExchangeValue>> {
        return exchangerService.exchangeRates(preferences.baseCurrency())
                .subscribeOn(ioScheduler)
                .flatMapObservable { initial ->
                    val initialRates = initial.rates
                            .map { rate -> rate.toExchangeRate(null) }
                            .toMutableList()
                            .addBaseCurrency(initial.base, null)
                    val orderedRates = ValuesHolder(initialRates, initial.base)

                    baseCurrencyUpdates.scan(orderedRates) { transfer, base ->
                        transfer.pinBaseCurrency(base)
                    }.switchMap { holder ->
                        Observables.combineLatest(
                                ratesUpdates(holder.base),
                                currencyValueUpdates(holder.values.first().value)
                        ) { rates, (baseValue) ->
                            updateValues(holder.values, rates, baseValue)
                        }
                    }
                }
                .observeOn(mainScheduler)
    }

    private fun updateValues(
            values: MutableList<ExchangeValue>,
            rates: Map<String, Rate>,
            baseValue: Float?
    ): List<ExchangeValue> {
        values.forEachIndexed { index, value ->
            val newValue = when {
                baseValue == null -> null
                index == 0 -> baseValue
                else -> (rates[value.currency]?.rate ?: 1f) * baseValue
            }
            values[index] = value.copy(value = newValue)
        }
        return values.toList()
    }

    private fun ratesUpdates(base: String): Observable<Map<String, Rate>> {
        return exchangerService.exchangeRates(base)
                .repeatWhen { it.delay(preferences.updateTime(), TimeUnit.SECONDS) }
                .retryWhen { errors ->
                    errors.switchMap { error ->
                        when (error) {
                            is IOException -> Flowable.timer(
                                    preferences.updateTime(),
                                    TimeUnit.SECONDS
                            )
                            else -> throw Exceptions.propagate(error)
                        }
                    }
                }
                .map { rates -> rates.rates.associateBy { it.currency } }
                .subscribeOn(ioScheduler)
                .toObservable()
    }

    private fun currencyValueUpdates(initial: Float?): Observable<Optional<Float>> {
        return currencyValueUpdates.mapNotNull { input ->
            when {
                input.isEmpty() -> None
                else -> input.toString()
                        .replace(Separator.EU, Separator.US)
                        .toFloatOrNull()
                        .toOptional()
            }
        }.startWith(initial.toOptional())
    }

    private fun Rate.toExchangeRate(value: Float?): ExchangeValue {
        return ExchangeValue(currency, value, flagsMapper.mapFlag(currency))
    }

    private fun MutableList<ExchangeValue>.addBaseCurrency(
            base: String,
            value: Float?
    ): MutableList<ExchangeValue> {
        add(0, ExchangeValue(base, value, flagsMapper.mapFlag(base)))
        return this
    }

    private fun ValuesHolder.pinBaseCurrency(base: String): ValuesHolder {
        values.find { it.currency == base }?.let {
            values.remove(it)
            values.add(0, it)
            this.base = it.currency
        }

        return this
    }

    fun updateCurrencyValue(value: CharSequence) {
        currencyValueUpdatesSubject.onNext(value)
    }

    fun updateBaseCurrency(value: String) {
        baseCurrencyUpdatesSubject.onNext(value)
    }

    private data class ValuesHolder(val values: MutableList<ExchangeValue>, var base: String)

}