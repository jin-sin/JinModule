package com.appknot.core_rx.binding

import androidx.databinding.PropertyChangeRegistry
import com.appknot.core_rx.annotation.BindingPropertyDelegate
import com.appknot.core_rx.extensions.BindingManager
import com.appknot.core_rx.extensions.bindingId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

/**
 *
 * @author Jin on 2021/07/07
 */

@BindingPropertyDelegate
fun <T> Flow<T>.asBindingProperty(coroutineScope: CoroutineScope, defaultValue: T) =
    FlowBindingPropertyIdWithDefaultValueOnScope(this, coroutineScope, defaultValue)

class FlowBindingPropertyIdWithDefaultValueOnScope<T> constructor(
    private val flow: Flow<T>,
    private val coroutineScope: CoroutineScope,
    private val defaultValue: T
) {
    operator fun provideDelegate(bindingObservable: BindingObservable, property: KProperty<*>): Delegate<T> {
        val bindingId = BindingManager.getBindingIdByProperty(property)
        val delegate = Delegate(defaultValue, coroutineScope, bindingId)
        delegate.collect(flow, bindingObservable)
        return delegate
    }

    class Delegate<T>(
        private var value: T,
        private val coroutineScope: CoroutineScope,
        private val bindingId: Int
    ) {
        fun collect(flow: Flow<T>, bindingObservable: BindingObservable) {
            coroutineScope.launch {
                flow.distinctUntilChanged().collect {
                    value = it
                    bindingObservable.notifyPropertyChanged(bindingId)
                }
            }
        }

        operator fun getValue(thisRef: Any, property: KProperty<*>): T = value
    }
}

@BindingPropertyDelegate
fun <T> StateFlow<T>.asBindingProperty(coroutineScope: CoroutineScope) =
    StateFlowBindingPropertyIdOnScope(coroutineScope, this)

class StateFlowBindingPropertyIdOnScope<T> constructor(
    private val coroutineScope: CoroutineScope,
    private val stateFlow: StateFlow<T>,
) {

    operator fun provideDelegate(bindingObservable: BindingObservable, property: KProperty<*>): Delegate<T> {
        val delegate = Delegate(stateFlow, coroutineScope, property.bindingId())
        delegate.collect(bindingObservable)
        return delegate
    }

    class Delegate<T>(
        private val stateFlow: StateFlow<T>,
        private val coroutineScope: CoroutineScope,
        private val bindingId: Int
    ) {
        fun collect(bindingObservable: BindingObservable) {
            coroutineScope.launch {
                stateFlow.collect {
                    bindingObservable.notifyPropertyChanged(bindingId)
                }
            }
        }

        operator fun getValue(thisRef: Any, property: KProperty<*>): T = stateFlow.value
    }
}