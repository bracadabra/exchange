package ru.bracadabra.exchange

import androidx.lifecycle.ViewModel
import io.reactivex.Single
import ru.bracadabra.exchange.echanger.ExchangerService
import ru.bracadabra.exchange.echanger.Rates

class ExchangeViewModel(private val exchangerService: ExchangerService) : ViewModel() {

    fun exchangeRates(base: String): Single<Rates> {
        return exchangerService.exchangeRates(base)
    }

}