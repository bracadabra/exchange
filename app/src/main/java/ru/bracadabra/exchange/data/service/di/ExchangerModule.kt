package ru.bracadabra.exchange.data.service.di

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import ru.bracadabra.exchange.data.service.ExchangerApi
import javax.inject.Qualifier

@Qualifier
annotation class ExchangerBaseUrl

@Module
object ExchangerModule {

    @JvmStatic
    @ExchangerBaseUrl
    @Provides
    fun provideBaseUrl(): String {
        return "https://revolut.duckdns.org"
    }

    @JvmStatic
    @Provides
    fun provideExchangerApi(builder: Retrofit.Builder, @ExchangerBaseUrl baseUrl: String): ExchangerApi {
        return builder.baseUrl(baseUrl)
            .build()
            .create(ExchangerApi::class.java)
    }

}