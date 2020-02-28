package com.appknot.core_rx.widget.timer

import android.os.SystemClock
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.appknot.core_rx.util.SingleLiveEvent
import java.util.*

/**
 *
 * @author Jin on 2020-02-24
 */

class TimerLiveData : SingleLiveEvent<Long>()    {

    var period = 1000L
    val initialTime: Long = SystemClock.elapsedRealtime()
    var timer: Timer? = null

    override fun onActive() {
        timer = Timer()
    }

    override fun onInactive() {
        timer?.cancel()
    }

    fun observe(owner: LifecycleOwner, observer: (Long) -> Unit) {
        super.observe(owner, androidx.lifecycle.Observer {
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val newValue = (SystemClock.elapsedRealtime() - initialTime) / 1000
                    if (it == period) {
                        if (it == newValue * 1000) {
                            observer(newValue)
                        }
                    } else {
                        observer(newValue)
                    }
                }
            }, it, period)
        })
    }
}