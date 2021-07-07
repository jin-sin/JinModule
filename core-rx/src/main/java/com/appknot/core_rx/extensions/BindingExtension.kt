package com.appknot.core_rx.extensions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 *
 * @author Jin on 2021/07/06
 */

fun <T : ViewDataBinding> ViewGroup.binding(
    @LayoutRes layoutRes: Int,
    attachToParent: Boolean = false
): T {
    return DataBindingUtil.inflate(
        LayoutInflater.from(context), layoutRes, this, attachToParent
    )
}

fun <T : ViewDataBinding> ViewGroup.binding(
    @LayoutRes layoutRes: Int,
    attachToParent: Boolean = false,
    block: T.() -> Unit
): T {
    return binding<T>(layoutRes, attachToParent).apply(block)
}

internal fun KProperty<*>.bindingId(): Int {
    return BindingManager.getBindingIdByProperty(this)
}

internal fun KFunction<*>.bindingId(): Int {
    return BindingManager.getBindingIdByFunction(this)
}