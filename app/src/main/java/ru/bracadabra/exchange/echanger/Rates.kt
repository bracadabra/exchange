package ru.bracadabra.exchange.echanger

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Rates(val base: String, val date: String, val rates: List<Rate>)

@JsonClass(generateAdapter = true)
data class Rate(val currency: String, val rate: Float)
