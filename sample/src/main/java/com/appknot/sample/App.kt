package com.appknot.sample

import android.app.Application
import android.content.Context
/**
 *
 * @author Jin on 2019-10-16
 */
class App : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        try {
//            MultiDex.install(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}