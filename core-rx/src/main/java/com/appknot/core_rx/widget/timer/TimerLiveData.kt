package com.appknot.core_rx.widget.timer

import android.os.SystemClock
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

/**
 *
 * @author Jin on 2020-02-24
 */

class TimerLiveData : MutableLiveData<Long>()    {

    var delay = 1000L
    var period = 1000L
    val initialTime: Long = SystemClock.elapsedRealtime()
    var timer: Timer? = null

    override fun onActive() {
        timer = Timer()
//        timer?.scheduleAtFixedRate(object : TimerTask() {
//            override fun run() {
//                val newValue = (SystemClock.elapsedRealtime() - initialTime) / 1000
//                postValue(newValue)
//            }
//        }, delay, period)
    }

    override fun onInactive() {
        timer?.cancel()
    }

    fun observe(owner: LifecycleOwner, observer: (Long) -> Unit) {
        super.observe(owner, androidx.lifecycle.Observer {
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val newValue = (SystemClock.elapsedRealtime() - initialTime) / 1000
//                    postValue(newValue)
                    observer(newValue)
                }
            }, it, period)
        })
    }
}