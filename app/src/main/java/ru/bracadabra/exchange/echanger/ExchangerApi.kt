package ru.bracadabra.exchange.echanger

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangerApi {

    @GET("latest")
    fun exchangeRates(@Query("base") base: String): Single<Rates>

}