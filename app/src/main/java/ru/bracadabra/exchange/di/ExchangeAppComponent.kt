package ru.bracadabra.exchange.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import ru.bracadabra.exchange.ExchangeApplication
import ru.bracadabra.exchange.data.service.di.ExchangerModule
import ru.bracadabra.exchange.ui.di.ExchangeModule
import javax.inject.Singleton


@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        NetworkModule::class,
        ExchangerModule::class,
        SchedulersModule::class,
        ViewModelFactoryModule::class,
        ExchangeModule::class
    ]
)
interface ExchangeAppComponent {

    fun inject(exchangeApplication: ExchangeApplication)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun bind(application: Application): Builder

        fun build(): ExchangeAppComponent

    }

}