package com.appknot.sample

import android.app.Application
import com.appknot.sample.module.apiModule
import com.appknot.sample.module.appContext
import com.appknot.sample.module.moshiModule
import com.appknot.sample.module.viewModelModule
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
                viewModelModule,
                apiModule,
                moshiModule
            ))
        }
    }
}