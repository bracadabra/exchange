package ru.bracadabra.exchange.data.service

import io.reactivex.Scheduler
import io.reactivex.Single
import ru.bracadabra.exchange.di.IoScheduler
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangerService @Inject constructor(
    private val api: ExchangerApi,
    @IoScheduler private val ioScheduler: Scheduler
) {

    fun exchangeRates(base: String): Single<Rates> {
        return api.exchangeRates(base).subscribeOn(ioScheduler)
    }
}