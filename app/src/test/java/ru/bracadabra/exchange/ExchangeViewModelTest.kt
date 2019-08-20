package ru.bracadabra.exchange


import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import ru.bracadabra.exchange.data.CurrenciesFlagsMapper
import ru.bracadabra.exchange.data.preference.Preferences
import ru.bracadabra.exchange.data.service.ExchangerApi
import ru.bracadabra.exchange.data.service.ExchangerService
import ru.bracadabra.exchange.data.service.Rate
import ru.bracadabra.exchange.data.service.Rates
import ru.bracadabra.exchange.ui.ExchangeValue
import ru.bracadabra.exchange.ui.ExchangeViewModel
import ru.bracadabra.exchange.ui.ExchangeViewState
import java.util.concurrent.TimeUnit

class ExchangeViewModelTest {

    private lateinit var exchangeViewModel: ExchangeViewModel
    private lateinit var testScheduler: TestScheduler
    private lateinit var exchangerApi: ExchangerApi

    @Before
    fun setup() {
        exchangerApi = mock(ExchangerApi::class.java)
        `when`(exchangerApi.exchangeRates("EUR")).thenReturn(Single.just(ratesForEur()))

        testScheduler = TestScheduler()

        val flagsMapper = mock(CurrenciesFlagsMapper::class.java)
        `when`(flagsMapper.mapFlag(anyString())).thenReturn(0)

        val preferences = mock(Preferences::class.java)
        `when`(preferences.baseCurrency()).thenReturn("EUR")
        `when`(preferences.updateTime()).thenReturn(1L)

        exchangeViewModel = ExchangeViewModel(
                ExchangerService(exchangerApi),
                testScheduler,
                testScheduler,
                Schedulers.trampoline(),
                flagsMapper,
                preferences
        )
    }

    @Test
    fun viewStates_progress() {
        val observer = exchangeViewModel.viewStates().test()
        testScheduler.triggerActions()

        observer.assertValueAt(0) { it is ExchangeViewState.Progress }
        observer.dispose()
    }

    @Test
    fun viewStates_ready() {
        val observer = exchangeViewModel.viewStates()
                .waitForReady()
                .test()
        testScheduler.triggerActions()

        val expected = ExchangeViewState.Ready(
                listOf(
                        ExchangeValue("EUR", null, 0),
                        ExchangeValue("AUD", null, 0)
                )
        )
        observer.assertValue(expected)
        observer.dispose()
    }

    @Test
    fun viewStates_updates() {
        `when`(exchangerApi.exchangeRates("EUR")).thenReturn(
                Single.just(ratesForEur(1.6212f)),
                Single.just(ratesForEur(1.6214f))
        )

        val observer = exchangeViewModel.viewStates()
                .waitForUpdate()
                .test()
        testScheduler.triggerActions()
        exchangeViewModel.updateCurrencyValue("10")
        testScheduler.advanceTimeBy(1, TimeUnit.SECONDS)

        val expected = arrayOf(
                ExchangeViewState.Ready(
                        listOf(
                                ExchangeValue("EUR", 10.0f, 0),
                                ExchangeValue("AUD", 16.212f, 0)
                        )
                ),
                ExchangeViewState.Ready(
                        listOf(
                                ExchangeValue("EUR", 10.0f, 0),
                                ExchangeValue("AUD", 16.214f, 0)
                        )
                )

        )
        observer.assertValues(*expected)
        observer.dispose()
    }

    @Test
    fun viewStates_error() {
        `when`(exchangerApi.exchangeRates("EUR")).thenReturn(Single.error(RuntimeException()))

        val observer = exchangeViewModel.viewStates()
                .waitForReady()
                .test()
        testScheduler.triggerActions()

        observer.assertValue(ExchangeViewState.Error(R.string.exchange_common_error))
        observer.dispose()
    }

    @Test
    fun updateCurrencyValue() {
        val observer = exchangeViewModel.viewStates()
                .waitForUpdate()
                .test()
        testScheduler.triggerActions()

        exchangeViewModel.updateCurrencyValue("10")
        testScheduler.triggerActions()

        val expected = ExchangeViewState.Ready(
                listOf(
                        ExchangeValue("EUR", 10.0f, 0),
                        ExchangeValue("AUD", 16.212f, 0)
                )
        )
        observer.assertValue(expected)
        observer.dispose()
    }

    @Test
    fun updateBaseCurrency() {
        `when`(exchangerApi.exchangeRates("AUD")).thenReturn(Single.just(ratesForAud()))

        val observer = exchangeViewModel.viewStates()
                .waitForUpdate()
                .test()
        testScheduler.triggerActions()

        exchangeViewModel.updateBaseCurrency("AUD")
        testScheduler.triggerActions()

        val expected = ExchangeViewState.Ready(
                listOf(
                        ExchangeValue("AUD", null, 0),
                        ExchangeValue("EUR", null, 0)
                )
        )
        observer.assertValue(expected)
        observer.dispose()
    }

    private fun ratesForEur(rate: Float = 1.6212f): Rates {
        return Rates(
                base = "EUR",
                date = "2018-09-06",
                rates = listOf(
                        Rate("AUD", rate)
                )
        )
    }

    private fun ratesForAud(): Rates {
        return Rates(
                base = "AUD",
                date = "2018-09-06",
                rates = listOf(
                        Rate("EUR", 0.61687f)
                )
        )
    }

    private fun Observable<ExchangeViewState>.waitForReady() = skip(1)

    private fun Observable<ExchangeViewState>.waitForUpdate() = skip(2)
}