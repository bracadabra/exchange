package ru.bracadabra.exchange.di

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Qualifier

@Qualifier
annotation class IoScheduler

@Qualifier
annotation class ComputationScheduler

@Qualifier
annotation class MainScheduler

@Module
object SchedulersModule {

    @JvmStatic
    @IoScheduler
    @Provides
    fun provideIoScheduler(): Scheduler {
        return Schedulers.io()
    }

    @JvmStatic
    @ComputationScheduler
    @Provides
    fun provideComputationScheduler(): Scheduler {
        return Schedulers.computation()
    }

    @JvmStatic
    @MainScheduler
    @Provides
    fun provideMainScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

}