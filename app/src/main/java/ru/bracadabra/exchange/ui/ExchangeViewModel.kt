package ru.bracadabra.exchange.ui

import androidx.lifecycle.ViewModel
import io.reactivex.Single
import ru.bracadabra.exchange.data.service.ExchangerService
import ru.bracadabra.exchange.data.service.Rates

class ExchangeViewModel(private val exchangerService: ExchangerService) : ViewModel() {

    fun exchangeRates(base: String): Single<Rates> {
        return exchangerService.exchangeRates(base)
    }

}