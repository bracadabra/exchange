package ru.bracadabra.exchange.ui

import androidx.annotation.StringRes

sealed class ExchangeViewState {

    data class Ready(val values: List<ExchangeValue>) : ExchangeViewState()

    object Progress : ExchangeViewState()

    data class Error(@StringRes val message: Int) : ExchangeViewState()

}