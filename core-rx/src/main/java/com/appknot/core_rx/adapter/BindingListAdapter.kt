package com.appknot.core_rx.adapter

import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appknot.core_rx.binding.BindingObservable
import com.appknot.core_rx.extensions.bindingId
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import androidx.databinding.library.baseAdapters.BR

/**
 *
 * @author Jin on 2021/07/07
 */

abstract class BindingListAdapter<T, VH : RecyclerView.ViewHolder> constructor(
    val callback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, VH>(callback), BindingObservable {

    private val lock: Any = Any()

    @get:Bindable
    var isSubmitted: Boolean = false
        private set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(::isSubmitted)
            }
        }

    private var propertyCallbacks: PropertyChangeRegistry? = null

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks
                ?: PropertyChangeRegistry().also { propertyCallbacks = it }
            propertyCallbacks.add(callback)
        }
    }

    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.remove(callback)
        }
    }

    override fun notifyPropertyChanged(property: KProperty<*>) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.notifyCallbacks(this, property.bindingId(), null)
        }
    }

    override fun notifyPropertyChanged(function: KFunction<*>) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.notifyCallbacks(this, function.bindingId(), null)
        }
    }

    override fun notifyPropertyChanged(bindingId: Int) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.notifyCallbacks(this, bindingId, null)
        }
    }

    override fun notifyAllPropertiesChanged() {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.notifyCallbacks(this, BR._all, null)
        }
    }

    override fun submitList(list: List<T>?) {
        super.submitList(list)
        isSubmitted = list != null
    }

    override fun submitList(list: List<T>?, commitCallback: Runnable?) {
        super.submitList(list, commitCallback)
        isSubmitted = list != null
    }

    override fun clearAllProperties() {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.clear()
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        clearAllProperties()
    }
}