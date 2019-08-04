package ru.bracadabra.exchange.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.android.support.AndroidSupportInjection
import ru.bracadabra.exchange.BaseFragment
import ru.bracadabra.exchange.R
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_exchange.exchange_rates_list as exchangeRatesList

class ExchangeFragment : BaseFragment() {

    @Inject
    protected lateinit var exchangeViewModel: ExchangeViewModel
    @Inject
    protected lateinit var adapter: ExchangeAdapter

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_exchange, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        exchangeRatesList.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        disposeOnStop(
                exchangeViewModel.exchangeRates().subscribe { rates ->
                    adapter.items = rates
                },

                adapter.valueChanges.subscribe { value ->
                    exchangeViewModel.updateCurrencyValue(value)
                }
        )
    }

}