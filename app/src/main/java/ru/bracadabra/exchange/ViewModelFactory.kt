package ru.bracadabra.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.MapKey
import javax.inject.Provider
import kotlin.reflect.KClass

@MapKey
@Target(AnnotationTarget.FUNCTION)
annotation class ViewModelKey(val value: KClass<out ViewModel>)

class ViewModelFactory(private val models: Map<Class<out ViewModel>, Provider<ViewModel>>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val provider = models[modelClass] ?: throw IllegalArgumentException("Model class $modelClass not found")

        @Suppress("UNCHECKED_CAST")
        return provider.get() as T
    }

}