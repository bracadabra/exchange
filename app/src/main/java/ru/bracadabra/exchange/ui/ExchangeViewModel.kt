package ru.bracadabra.exchange.ui

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import ru.bracadabra.exchange.data.CurrenciesFlagsMapper
import ru.bracadabra.exchange.data.service.ExchangerService
import ru.bracadabra.exchange.utils.extensions.mapNotNull
import java.util.concurrent.TimeUnit

class ExchangeViewModel(
        private val exchangerService: ExchangerService,
        private val ioScheduler: Scheduler,
        private val mainScheduler: Scheduler,
        private val flagsMapper: CurrenciesFlagsMapper
) : ViewModel() {

    private val currencyValueUpdatesSubject = PublishSubject.create<CharSequence>()
    private val currencyValueUpdates: Observable<Float> = currencyValueUpdatesSubject.mapNotNull {
        if (it.isEmpty()) 0f else it.toString().toFloatOrNull()
    }

    fun exchangeRates(): Observable<out List<ExchangeRate>> {
        return Observables.combineLatest(
                exchangerService.exchangeRates(DUMMY_BASE)
                        .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
                        .toObservable(),
                currencyValueUpdates.startWith(0f)
        ) { rates, value ->
            rates.rates
                    .map {
                        ExchangeRate.Target(
                                it.currency,
                                it.rate * value,
                                flagsMapper.mapFlag(it.currency)
                        )
                    }
                    .toMutableList<ExchangeRate>()
                    .apply {
                        add(
                                0,
                                ExchangeRate.Base(
                                        DUMMY_BASE,
                                        value,
                                        flagsMapper.mapFlag(DUMMY_BASE)
                                )
                        )
                    }
        }
                .distinctUntilChanged()
                .subscribeOn(ioScheduler)
                .observeOn(mainScheduler)
    }

    fun updateCurrencyValue(value: CharSequence) {
        currencyValueUpdatesSubject.onNext(value)
    }

    companion object {
        const val DUMMY_BASE = "EUR"
    }

}