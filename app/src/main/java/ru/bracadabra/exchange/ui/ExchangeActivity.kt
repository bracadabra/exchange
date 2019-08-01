package ru.bracadabra.exchange.ui

import android.os.Bundle
import ru.bracadabra.exchange.BaseActivity
import ru.bracadabra.exchange.R

class ExchangeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, ExchangeFragment())
                .commit()
        }
    }
}
