package com.appknot.module.widget.camera.base

import android.view.Surface
import android.view.SurfaceHolder
import android.view.View


abstract class PreviewImpl {

    private var callback: Callback? = null

    var width: Int = 0
        private set

    var height: Int = 0
        private set

    abstract val surface: Surface

    abstract val view: View

    abstract val outputClass: Class<*>

    abstract val isReady: Boolean

    val surfaceHolder: SurfaceHolder?
        get() = null

    open val surfaceTexture: Any?
        get() = null

    interface Callback {
        fun onSurfaceChanged()
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    abstract fun setDisplayOrientation(displayOrientation: Int)

    protected fun dispatchSurfaceChanged() {
        callback?.onSurfaceChanged()
    }

    open fun setBufferSize(width: Int, height: Int) {}

    fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

}
