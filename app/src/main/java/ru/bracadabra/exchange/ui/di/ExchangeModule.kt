package ru.bracadabra.exchange.ui.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import ru.bracadabra.exchange.ViewModelFactory
import ru.bracadabra.exchange.ViewModelKey
import ru.bracadabra.exchange.data.service.ExchangerService
import ru.bracadabra.exchange.ui.ExchangeFragment
import ru.bracadabra.exchange.ui.ExchangeViewModel

@Module(
    includes = [
        ExchangeModule.ViewModelProvider::class
    ]
)
interface ExchangeModule {

    @ContributesAndroidInjector(
        modules = [
            ViewModelInjector::class
        ]
    )
    fun bind(): ExchangeFragment

    @Module
    object ViewModelProvider {

        @JvmStatic
        @Provides
        @IntoMap
        @ViewModelKey(ExchangeViewModel::class)
        fun provideExchangeViewModel(exchangerService: ExchangerService): ViewModel {
            return ExchangeViewModel(exchangerService)
        }
    }

    @Module
    object ViewModelInjector {

        @JvmStatic
        @Provides
        fun provideExchangeViewModel(
            factory: ViewModelFactory,
            fragment: ExchangeFragment
        ): ExchangeViewModel {
            return ViewModelProviders.of(fragment, factory).get(ExchangeViewModel::class.java)
        }
    }

}