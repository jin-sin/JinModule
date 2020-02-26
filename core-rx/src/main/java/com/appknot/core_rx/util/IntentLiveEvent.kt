package com.appknot.core_rx.util

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

/**
 *
 * @author Jin on 2020/02/26
 */

class IntentLiveEvent : SingleLiveEvent<Intent>()   {

    fun observe(owner: LifecycleOwner, observer: (Intent) -> Unit) {
        super.observe(owner, Observer {
            it?.run {
                observer(it)
            }
        })
    }
}