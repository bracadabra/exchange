package ru.bracadabra.exchange.data.preference

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Preferences @Inject constructor() {

    fun baseCurrency(): String {
        return "EUR"
    }

    fun updateTime(): Long {
        return 1L
    }
}