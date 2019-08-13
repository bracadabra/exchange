package ru.bracadabra.exchange.ui

import android.app.Activity
import android.os.Build
import android.os.Parcelable
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.focusChanges
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.parcel.Parcelize
import ru.bracadabra.exchange.R
import ru.bracadabra.exchange.utils.Separator
import java.text.DecimalFormatSymbols
import java.util.*
import javax.inject.Inject
import kotlinx.android.synthetic.main.item_exchange.exchange_currency_code as codeView
import kotlinx.android.synthetic.main.item_exchange.exchange_currency_flag as flagView
import kotlinx.android.synthetic.main.item_exchange.exchange_currency_title as titleView
import kotlinx.android.synthetic.main.item_exchange.exchange_currency_value as valueView

@Parcelize
data class ExchangeValue(val currency: String, val value: Float?, val flag: Int) : Parcelable

class ExchangeAdapter @Inject constructor(
        context: Activity
) : RecyclerView.Adapter<ExchangeAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    private val filters: Array<InputFilter> =
            arrayOf(SeparatorFixInputFilter(), CurrencyInputFilter())

    private val valueChangesSubject = PublishSubject.create<CharSequence>()
    private val focusChangesSubject = PublishSubject.create<String>()

    val valueChanges: Observable<CharSequence> = valueChangesSubject
    val focusChanges: Observable<String> = focusChangesSubject

    var items: List<ExchangeValue> = emptyList()
        set(value) {
            ExchangeRatesDiffUtilCallback(field, value).dispatchesTo(this)
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_exchange, parent, false)).apply {
            valueView.textChanges()
                    .filter { !freezeUpdate }
                    .filter { adapterPosition == 0 }
                    .subscribe(valueChangesSubject)
            valueView.focusChanges()
                    .filter { adapterPosition > 0 }
                    .filter { it }
                    .doOnNext {
                        valueView.post { valueView.setSelection(valueView.length()) }
                    }
                    .map { currentItem.currency }
                    .subscribe(focusChangesSubject)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        val rate = items[position]
        val payload = payloads.lastOrNull() as ExchangeRatesPayload?
        holder.bind(rate, payload)

        holder.valueView.filters = if (position == 0) {
            filters
        } else {
            emptyArray()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

        override val containerView: View = itemView

        lateinit var currentItem: ExchangeValue

        var freezeUpdate = false

        fun bind(rate: ExchangeValue, payload: ExchangeRatesPayload?) {
            currentItem = rate

            codeView.text = rate.currency
            titleView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Currency.getInstance(rate.currency).getDisplayName(Locale.ENGLISH)
            } else {
                rate.currency
            }
            flagView.setImageResource(rate.flag)

            if (!valueView.isFocused) {
                val formattedValue = when {
                    payload?.value == 0f || rate.value == null -> null
                    else -> formatValue(rate.value)
                }
                valueView.setTextSilently(formattedValue)
            }
        }

        private fun EditText.setTextSilently(text: CharSequence?) {
            freezeUpdate = true
            setText(text)
            freezeUpdate = false
        }

        private fun formatValue(value: Float): String {
            return if (value % 1f == 0f) {
                value.toInt().toString()
            } else {
                String.format("%.2f", value)
            }
        }
    }
}

data class ExchangeRatesPayload(val value: Float)

private class SeparatorFixInputFilter : InputFilter {

    private val useDefault = DecimalFormatSymbols.getInstance().decimalSeparator == Separator.US

    override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
    ): CharSequence {
        return if (useDefault) {
            source
        } else {
            source.toString().replace(Separator.US, Separator.EU)
        }
    }
}

private class CurrencyInputFilter : InputFilter {

    override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
    ): CharSequence? {
        val index = SEPARATORS.map { dest.indexOf(it) }.filter { it >= 0 }.max()
        return if (index == null || dest.length - index <= DECIMAL_SIZE) {
            source
        } else {
            ""
        }
    }

    companion object {
        private val SEPARATORS = listOf(Separator.EU, Separator.US)
        private const val DECIMAL_SIZE = 2
    }
}

private class ExchangeRatesDiffUtilCallback(
        private val oldList: List<ExchangeValue>,
        private val newList: List<ExchangeValue>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return oldList[oldPosition].currency == newList[newPosition].currency
    }

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return oldList[oldPosition].value == newList[newPosition].value
    }

    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any {
        return ExchangeRatesPayload(newList[newPosition].value ?: 0f)
    }

    fun dispatchesTo(adapter: RecyclerView.Adapter<*>) {
        DiffUtil.calculateDiff(this, true).dispatchUpdatesTo(adapter)
    }

}