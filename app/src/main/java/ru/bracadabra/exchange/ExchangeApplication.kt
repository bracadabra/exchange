package ru.bracadabra.exchange

import android.app.Application
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import ru.bracadabra.exchange.di.DaggerExchangeAppComponent
import javax.inject.Inject


class ExchangeApplication : Application(), HasSupportFragmentInjector {

    @Inject
    protected lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate() {
        super.onCreate()

        DaggerExchangeAppComponent.builder()
            .bind(this)
            .build()
            .inject(this)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }
}
