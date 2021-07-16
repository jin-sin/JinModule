package com.appknot.core_rx.binding

import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.SavedStateHandle
import com.appknot.core_rx.annotation.BindingPropertyDelegate
import com.appknot.core_rx.extensions.bindingId
import kotlin.reflect.KProperty

/**
 *
 * @author Jin on 2021/07/07
 */

@BindingPropertyDelegate
fun <T> bindingProperty(defaultValue: T) =
    BindingPropertyIdWithDefaultValue(defaultValue)

class BindingPropertyIdWithDefaultValue<T>(
    private var value: T
) {
    operator fun getValue(bindingObservable: BindingObservable, property: KProperty<*>): T = value

    operator fun setValue(bindingObservable: BindingObservable, property: KProperty<*>, value: T) {
        if (this.value != value) {
            this.value = value
            bindingObservable.notifyPropertyChanged(property.bindingId())
        }
    }
}

@BindingPropertyDelegate
fun <T> SavedStateHandle.asBindingProperty(key: String) =
    SavedStateHandleBindingProperty<T>(this, key)

class SavedStateHandleBindingProperty<T>(
    private val savedStateHandle: SavedStateHandle,
    private var key: String
) {
    operator fun getValue(bindingObservable: BindingObservable, property: KProperty<*>): T? = savedStateHandle.get<T?>(key)

    operator fun setValue(bindingObservable: BindingObservable, property: KProperty<*>, value: T?) {
        savedStateHandle.set(key, value)
        bindingObservable.notifyPropertyChanged(property.bindingId())
    }
}