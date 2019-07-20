package ru.bracadabra.exchange.echanger

import android.app.Application
import com.squareup.moshi.Moshi
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File

class ExchangerService(context: Application) {

    private val api = provideExchangerApi(
            provideOkHttp(context),
            provideMoshi()
    )

    fun exchangeRates(base: String): Single<Rates> {
        return api.exchangeRates(base).subscribeOn(Schedulers.io())
    }

    private fun provideOkHttp(context: Application): OkHttpClient {
        val cacheDir = File(context.cacheDir, "okhttp")
        val cache = Cache(cacheDir, (10 * 1024 * 1024).toLong())

        return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .cache(cache)
                .build()
    }

    private fun provideMoshi(): Moshi {
        return Moshi.Builder()
                .add(RatesAdapter())
                .build()
    }

    private fun provideExchangerApi(client: OkHttpClient, moshi: Moshi): ExchangerApi {
        return Retrofit.Builder()
                .baseUrl("https://revolut.duckdns.org")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ExchangerApi::class.java)
    }

}