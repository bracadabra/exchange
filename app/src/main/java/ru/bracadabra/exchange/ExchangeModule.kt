package ru.bracadabra.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import ru.bracadabra.exchange.echanger.ExchangerService

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