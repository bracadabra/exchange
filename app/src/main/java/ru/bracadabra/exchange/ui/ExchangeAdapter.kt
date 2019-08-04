package ru.bracadabra.exchange.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.subjects.PublishSubject
import kotlinx.android.extensions.LayoutContainer
import ru.bracadabra.exchange.R
import javax.inject.Inject
import kotlinx.android.synthetic.main.item_exchange.exchange_currency_code as codeView
import kotlinx.android.synthetic.main.item_exchange.exchange_currency_flag as flagView
import kotlinx.android.synthetic.main.item_exchange.exchange_currency_title as titleView
import kotlinx.android.synthetic.main.item_exchange.exchange_currency_value as valueView


sealed class ExchangeRate {

    abstract val currency: String
    abstract val value: Float
    abstract val flag: Int

    data class Base(
            override val currency: String,
            override val value: Float,
            override val flag: Int
    ) : ExchangeRate()

    data class Target(
            override val currency: String,
            override val value: Float,
            override val flag: Int
    ) : ExchangeRate()

}

class ExchangeAdapter @Inject constructor(
        baseDelegate: BaseExchangeAdapterDelegate,
        targetDelegate: TargetExchangeAdapterDelegate
) : ListDelegationAdapter<List<ExchangeRate>>() {

    val valueChanges = baseDelegate.valueChanges

    init {
        delegatesManager.apply {
            addDelegate(baseDelegate)
            addDelegate(targetDelegate)
        }
        super.setItems(emptyList())
    }

    override fun setItems(items: List<ExchangeRate>) {
        ExchangeRatesDiffUtilCallback(this.items, items).dispatchesTo(this)
        super.setItems(items)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {

        override val containerView: View = itemView

        fun bind(rate: ExchangeRate) {
            codeView.text = rate.currency
            titleView.text = rate.currency
            flagView.setImageResource(rate.flag)
        }
    }
}

class BaseExchangeAdapterDelegate @Inject constructor(context: Activity) :
        AbsListItemAdapterDelegate<ExchangeRate, ExchangeRate, ExchangeAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    private val valueChangesSubject = PublishSubject.create<CharSequence>()
    val valueChanges = valueChangesSubject

    override fun onCreateViewHolder(parent: ViewGroup): ExchangeAdapter.ViewHolder {
        return ExchangeAdapter.ViewHolder(inflater.inflate(R.layout.item_exchange, parent, false))
                .apply {
                    valueView.textChanges().subscribe(valueChangesSubject)
                }
    }

    override fun isForViewType(
            item: ExchangeRate,
            items: List<ExchangeRate>,
            position: Int
    ): Boolean {
        return item is ExchangeRate.Base
    }

    override fun onBindViewHolder(
            item: ExchangeRate,
            holder: ExchangeAdapter.ViewHolder,
            payloads: List<Any>
    ) {
        holder.bind(item)
    }
}

class TargetExchangeAdapterDelegate @Inject constructor(context: Activity) :
        AbsListItemAdapterDelegate<ExchangeRate, ExchangeRate, ExchangeAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup): ExchangeAdapter.ViewHolder {
        return ExchangeAdapter.ViewHolder(inflater.inflate(R.layout.item_exchange, parent, false))
    }

    override fun isForViewType(
            item: ExchangeRate,
            items: List<ExchangeRate>,
            position: Int
    ): Boolean {
        return item is ExchangeRate.Target
    }

    override fun onBindViewHolder(
            item: ExchangeRate,
            holder: ExchangeAdapter.ViewHolder,
            payloads: List<Any>
    ) {
        holder.bind(item)

        val payload = payloads.lastOrNull() as ExchangeRatesPayload?
        if (payload == null || payload.value == 0f) {
            holder.valueView.text = null
        } else {
            holder.valueView.setText(String.format("%.2f", payload.value))
        }
    }
}

data class ExchangeRatesPayload(val value: Float)

class ExchangeRatesDiffUtilCallback(
        private val oldList: List<ExchangeRate>,
        private val newList: List<ExchangeRate>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return oldList[oldPosition].currency == newList[newPosition].currency
    }

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        return if (areItemsTheSame(oldPosition, newPosition)) {
            oldList[oldPosition].value == newList[newPosition].value
        } else {
            false
        }
    }

    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any {
        return ExchangeRatesPayload(newList[newPosition].value)
    }

    fun dispatchesTo(adapter: RecyclerView.Adapter<*>) {
        DiffUtil.calculateDiff(this).dispatchUpdatesTo(adapter)
    }

}