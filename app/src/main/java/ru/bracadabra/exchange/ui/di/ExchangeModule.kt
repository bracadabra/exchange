package ru.bracadabra.exchange.ui.di

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import io.reactivex.Scheduler
import ru.bracadabra.exchange.ViewModelFactory
import ru.bracadabra.exchange.ViewModelKey
import ru.bracadabra.exchange.data.CurrenciesFlagsMapper
import ru.bracadabra.exchange.data.service.ExchangerService
import ru.bracadabra.exchange.di.IoScheduler
import ru.bracadabra.exchange.di.MainScheduler
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
            ViewModelInjector::class,
            DependenciesProvider::class
        ]
    )
    fun bind(): ExchangeFragment

    @Module
    object ViewModelProvider {

        @JvmStatic
        @Provides
        @IntoMap
        @ViewModelKey(ExchangeViewModel::class)
        fun provideExchangeViewModel(
                exchangerService: ExchangerService,
                @IoScheduler ioScheduler: Scheduler,
                @MainScheduler mainScheduler: Scheduler,
                flagsMapper: CurrenciesFlagsMapper
        ): ViewModel {
            return ExchangeViewModel(exchangerService, ioScheduler, mainScheduler, flagsMapper)
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

    @Module
    object DependenciesProvider {

        @JvmStatic
        @Provides
        fun provideActivity(fragment: ExchangeFragment): Activity {
            return fragment.requireActivity()
        }

    }

}