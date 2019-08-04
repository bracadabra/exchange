package ru.bracadabra.exchange

import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

open class BaseFragment : Fragment() {

    private val disposeOnStop = CompositeDisposable()

    override fun onStop() {
        disposeOnStop.clear()
        super.onStop()
    }

    fun disposeOnStop(vararg disposables: Disposable) {
        disposeOnStop.addAll(*disposables)
    }

}