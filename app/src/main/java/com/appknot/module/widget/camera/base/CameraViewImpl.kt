package com.appknot.module.widget.camera.base

import android.view.View

/**
 *
 * @author Jin on 2019-08-23
 */

abstract class CameraViewImpl(protected val mCallback: Callback, protected val mPreview: PreviewImpl) {

    val view: View
        get() = mPreview.view

    abstract val isCameraOpened: Boolean

    abstract var facing: Int

    abstract val supportedAspectRatios: Set<AspectRatio>

    abstract var aspectRatio: AspectRatio

    abstract var autoFocus: Boolean

    abstract var flash: Int

    abstract fun start()

    abstract fun stop()

    abstract fun takePicture()

    abstract fun setDisplayOrientation(displayOrientation: Int)

    interface Callback {

        fun onCameraOpened()

        fun onCameraClosed()

        fun onPictureTaken(data: ByteArray)

    }

}
