package com.appknot.core_rx.widget.timer

import android.os.Handler
import android.os.SystemClock
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.appknot.core_rx.util.SingleLiveEvent
import java.util.*

/**
 *
 * @author Jin on 2020-02-24
 */

class TimerLiveData : SingleLiveEvent<Long>() {

    var millisInFuture = 1000L
    var countDownInterval = 1000L
    var counter: Runnable? = null
    lateinit var handler: Handler

    override fun onInactive() {
        stopTimer()
    }

    fun stopTimer() {
        counter?.let { handler?.removeCallbacks(it) }

    }

    fun observe(
        owner: LifecycleOwner,
        finishObserver: (Long) -> Unit,
        tickObserver: (Long) -> Unit
    ) {
        handler = Handler()
        super.observe(owner, Observer {
            counter = Runnable {
                it?.run {
                    if (millisInFuture <= 0) {
                        //Done
                        finishObserver(millisInFuture)
                        stopTimer()
                    } else {
                        tickObserver(millisInFuture)
                        millisInFuture -= countDownInterval
                        handler.postDelayed(counter, countDownInterval)
                    }
                }
            }

            handler.post(counter)

        })
    }
}