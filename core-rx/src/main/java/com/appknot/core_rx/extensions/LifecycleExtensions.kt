package com.appknot.core_rx.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver

/**
 *
 * @author Jin on 2019-06-10
 */

operator fun Lifecycle.plusAssign(observer: LifecycleObserver) = this.addObserver(observer)