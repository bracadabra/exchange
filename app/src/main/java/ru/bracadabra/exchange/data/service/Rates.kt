package ru.bracadabra.exchange.data.service

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
data class Rates(val base: String, val date: String, val rates: List<Rate>)

@Parcelize
@JsonClass(generateAdapter = true)
data class Rate(val currency: String, val rate: Float) : Parcelable
