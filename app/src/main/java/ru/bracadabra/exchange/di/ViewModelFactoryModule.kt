package ru.bracadabra.exchange.di

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import ru.bracadabra.exchange.ViewModelFactory
import javax.inject.Provider

@Module
object ViewModelFactoryModule {

    @JvmStatic
    @Provides
    fun provideViewModelFactory(
        providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>
    ): ViewModelFactory {
        return ViewModelFactory(providers)
    }

}
