package com.appknot.sample.module

import com.appknot.sample.viewmodel.TimerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 *
 * @author Jin on 2020-02-25
 */

val appContext = module {
    single(named("appContext")) { androidContext() }
}

val timerModule = module {
    viewModel { TimerViewModel() }
}