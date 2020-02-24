package com.appknot.core_rx.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 *
 * @author Jin on 2020-02-24
 */

class ToastMessage : SingleLiveEvent<Int>() {

    fun observe(owner: LifecycleOwner, observer: (Int) -> Unit) {
        super.observe(owner, Observer {
            it?.run {
                observer(it)
            }
        })
    }
}

class ToastMessageString : SingleLiveEvent<String>() {

    fun observe(owner: LifecycleOwner, observer: (String) -> Unit) {
        super.observe(owner, Observer {
            it?.run {
                observer(it)
            }
        })
    }
}