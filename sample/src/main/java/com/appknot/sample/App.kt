package com.appknot.sample

import android.app.Application
import android.content.Context
import com.appknot.sample.module.appContext
import com.appknot.sample.module.timerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.core.context.startKoin

/**
 *
 * @author Jin on 2019-10-16
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            androidFileProperties()
            modules(listOf(
                appContext,
                timerModule
            ))
        }
    }
}