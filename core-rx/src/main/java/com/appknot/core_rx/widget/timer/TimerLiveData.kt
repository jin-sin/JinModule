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
    var handler: Handler? = null
    var timerObserver: Observer<Long>? = null

    override fun onInactive() {
        stopTimer()
    }

    fun stopTimer() {
        counter?.let { handler?.removeCallbacks(it) }
    }

    fun removeTimerObserver() {
        timerObserver?.let {
            super.removeObserver(it)
            timerObserver = null
        }
    }

    fun observe(
        owner: LifecycleOwner,
        finishObserver: (Long) -> Unit,
        tickObserver: (Long) -> Unit
    ) {
        super.observe(owner, Observer {
            it?.let {
                runCounter(finishObserver, tickObserver)
            }
        })
    }

    /**
     * 이 observe 는 죽지않습니다.
     * 사용이 끝나면
     * {@link #removeTimerObserver(Observer)} 를 호출하여 Observer 를 지워주세요
     */
    fun observeForever(
        finishObserver: (Long) -> Unit,
        tickObserver: (Long) -> Unit
    ) {
        timerObserver = Observer {
            it?.let {
                runCounter(finishObserver, tickObserver)
            }
        }
        super.observeForever(timerObserver!!)
    }

    private fun runCounter(
        finishObserver: (Long) -> Unit,
        tickObserver: (Long) -> Unit
    ) {
        if (handler == null)
            handler = Handler()

        counter = Runnable {
            if (millisInFuture <= 0) {
                //Done
                finishObserver(millisInFuture)
                stopTimer()
            } else {
                tickObserver(millisInFuture)
                millisInFuture -= countDownInterval
                handler?.postDelayed(counter, countDownInterval)
            }
        }

        handler?.post(counter)
    }
}