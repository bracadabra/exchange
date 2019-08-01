package ru.bracadabra.exchange.di

import android.app.Application
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.bracadabra.exchange.echanger.RatesAdapter
import java.io.File

@Module
object NetworkModule {

    @JvmStatic
    @Provides
    fun provideOkHttpClient(context: Application): OkHttpClient {
        val cacheDir = File(context.cacheDir, "okhttp")
        val cache = Cache(cacheDir, (10 * 1024 * 1024).toLong())

        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .cache(cache)
            .build()
    }

    @JvmStatic
    @Provides
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(RatesAdapter())
            .build()
    }

    @JvmStatic
    @Provides
    fun provideRetrofitBuilder(client: OkHttpClient, moshi: Moshi): Retrofit.Builder {
        return Retrofit.Builder()
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
    }
}