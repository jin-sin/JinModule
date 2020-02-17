package com.appknot.core_rx.rx

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.disposables.Disposable

/**
 *
 * @author Jin on 2019-06-10
 */

class AutoActivatedDisposable(
    private val lifecycleOwner: LifecycleOwner,
    private val func: () -> Disposable
)
    : LifecycleObserver {

    private var disposable: Disposable? = null

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun activate() {
        disposable = func.invoke()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun deactivate() {
        disposable?.dispose()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun detachSelf() {
        lifecycleOwner.lifecycle.removeObserver(this)
    }
}