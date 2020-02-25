package com.appknot.sample

import com.appknot.core_rx.base.RxBaseActivity
import com.appknot.sample.databinding.ActivityTimerBinding
import com.appknot.sample.viewmodel.TimerViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TimerActivity : RxBaseActivity<ActivityTimerBinding, TimerViewModel>() {

    override val layoutResourceId: Int
        get() = R.layout.activity_timer
    override val viewModel by viewModel<TimerViewModel>()

    override fun initStartView() {
    }

    override fun initDataBinding() {
        viewModel.observeTimer(this)    {
            finish()
        }
        viewModel.startTimer(2000L, 2000L)
    }

    override fun initAfterBinding() {
    }
}
