package ru.bracadabra.exchange.data

import android.app.Application
import androidx.annotation.DrawableRes
import dagger.Reusable
import ru.bracadabra.exchange.R
import javax.inject.Inject

@Reusable
class CurrenciesFlagsMapper @Inject constructor(
        context: Application
) {

    private val resources = context.resources
    private val packageName = context.packageName

    @DrawableRes
    fun mapFlag(currencyCode: String): Int {
        return resources.getIdentifier("_${currencyCode.toLowerCase()}", "drawable", packageName)
                .let {
                    if (it == 0) R.drawable.fallback_flag else it
                }
    }

}