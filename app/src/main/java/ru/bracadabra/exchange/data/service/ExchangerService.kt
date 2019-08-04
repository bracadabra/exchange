package ru.bracadabra.exchange.data.service

import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangerService @Inject constructor(
        private val api: ExchangerApi
) {

    fun exchangeRates(base: String): Single<Rates> {
        return api.exchangeRates(base)
    }
}