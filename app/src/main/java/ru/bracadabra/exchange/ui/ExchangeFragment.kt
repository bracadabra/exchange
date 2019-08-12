package ru.bracadabra.exchange.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import ru.bracadabra.exchange.BaseFragment
import ru.bracadabra.exchange.R
import javax.inject.Inject
import kotlinx.android.synthetic.main.fragment_exchange.exchange_container as container
import kotlinx.android.synthetic.main.fragment_exchange.exchange_progress as progressView
import kotlinx.android.synthetic.main.fragment_exchange.exchange_rates_list as exchangeRatesList
import kotlinx.android.synthetic.main.fragment_exchange.exchange_toolbar as toolbar


class ExchangeFragment : BaseFragment() {

    @Inject
    protected lateinit var exchangeViewModel: ExchangeViewModel
    @Inject
    protected lateinit var adapter: ExchangeAdapter

    private val transition = AutoTransition().excludeChildren(exchangeRatesList, true)
    private val toolbarElevationListener = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val itemView = recyclerView.findViewHolderForAdapterPosition(0)?.itemView
            toolbar.isSelected = !(itemView != null && itemView.top - recyclerView.paddingTop == 0)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_exchange, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        exchangeRatesList.adapter = adapter
        exchangeRatesList.addOnScrollListener(toolbarElevationListener)
    }

    override fun onStart() {
        super.onStart()
        disposeOnStop(
                exchangeViewModel.viewStates().subscribe(::render),

                adapter.valueChanges.subscribe { value ->
                    exchangeViewModel.updateCurrencyValue(value)
                },

                adapter.focusChanges.subscribe { currency ->
                    exchangeViewModel.updateBaseCurrency(currency)
                }
        )
    }

    private fun render(state: ExchangeViewState) {
        when (state) {
            is ExchangeViewState.Ready -> {
                if (exchangeRatesList.isGone) {
                    TransitionManager.beginDelayedTransition(container, transition)
                    progressView.visibility = View.GONE
                    exchangeRatesList.visibility = View.VISIBLE
                }
                adapter.items = state.values
            }
            is ExchangeViewState.Progress -> {
                TransitionManager.beginDelayedTransition(container, transition)
                progressView.visibility = View.VISIBLE
                exchangeRatesList.visibility = View.GONE
            }
            is ExchangeViewState.Error -> {
                TransitionManager.beginDelayedTransition(container, transition)
                progressView.visibility = View.GONE
                Snackbar.make(container, state.message, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.exchange_error_retry) { exchangeViewModel.retryRequest() }
                        .show()
            }
        }
    }

}