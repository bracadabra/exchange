package ru.bracadabra.exchange

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ExchangeActivity : AppCompatActivity() {

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
