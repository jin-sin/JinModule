package com.appknot.core_rx.widget

import android.os.Handler

/**
 *
 * @author Jin on 2019-08-08
 */
abstract class AKCountDownTimer(pMillisInFuture: Long, pCountDownInterval: Long) {
    private var millisInFuture = pMillisInFuture
    private var countDownInterval = pCountDownInterval
    lateinit var counter: Runnable
    lateinit var handler: Handler
//    abstract var onFinishListener: ((Long) -> Unit)
//    abstract var onTickListener: ((Long) -> Unit)

    fun start() {
        handler = Handler()
        counter = Runnable {
            if (millisInFuture <= 0) {
                //Done
                onFinish()
            } else {
                onTick(millisInFuture)
                millisInFuture -= countDownInterval
                handler.postDelayed(counter, countDownInterval)
            }
        }

        handler.post(counter)
    }

    fun cancel() {
        handler.removeCallbacks(counter)
    }

    abstract fun onTick(millisUntilFinished: Long)
    abstract fun onFinish()
}