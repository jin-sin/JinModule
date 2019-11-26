package com.appknot.module.widget.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.util.Log
import android.util.SparseIntArray
import com.appknot.module.widget.camera.base.*
import java.util.*


/**
 *
 * @author Jin on 2019-08-23
 */

class AKCamera(callback: Callback, preview: PreviewImpl, context: Context) :
    CameraViewImpl(callback, preview) {

    private val mCameraManager: CameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val mCameraDeviceCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(camera: CameraDevice) {
            mCamera = camera
            mCallback.onCameraOpened()
            startCaptureSession()
        }

        override fun onClosed(camera: CameraDevice) {
            mCallback.onCameraClosed()
        }

        override fun onDisconnected(camera: CameraDevice) {
            mCamera = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.e(TAG, "onError: " + camera.id + " (" + error + ")")
            mCamera = null
        }

    }

    private val mSessionCallback = object : CameraCaptureSession.StateCallback() {

        override fun onConfigured(session: CameraCaptureSession) {
            if (mCamera == null) {
                return
            }
            mCaptureSession = session
            updateAutoFocus()
            updateFlash()
            try {
                mCaptureSession?.setRepeatingRequest(
                    mPreviewRequestBuilder!!.build(),
                    mCaptureCallback, null
                )
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Failed to start camera preview because it couldn't access camera", e)
            } catch (e: IllegalStateException) {
                Log.e(TAG, "Failed to start camera preview.", e)
            }

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e(TAG, "Failed to configure capture session.")
        }

        override fun onClosed(session: CameraCaptureSession) {
            if (mCaptureSession != null && mCaptureSession == session) {
                mCaptureSession = null
            }
        }

    }

    private val mCaptureCallback = object : PictureCaptureCallback() {

        override fun onPrecaptureRequired() {
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            setState(AKCamera.PictureCaptureCallback.STATE_PRECAPTURE)
            try {
                mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), this, null)
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE
                )
            } catch (e: CameraAccessException) {
                Log.e(TAG, "Failed to run precapture sequence.", e)
            }

        }

        override fun onReady() {
            captureStillPicture()
        }

    }

    private val mOnImageAvailableListener = object : ImageReader.OnImageAvailableListener {

        override fun onImageAvailable(reader: ImageReader) {
            reader.acquireNextImage().use { image ->
                val planes = image.planes
                if (planes.isNotEmpty()) {
                    val buffer = planes[0].buffer
                    val data = ByteArray(buffer.remaining())
                    buffer.get(data)
                    mCallback.onPictureTaken(data)
                }
            }
        }

    }


    private var mCameraId: String? = null

    private var mCameraCharacteristics: CameraCharacteristics? = null

    private var mCamera: CameraDevice? = null

    private var mCaptureSession: CameraCaptureSession? = null

    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null

    private var mImageReader: ImageReader? = null

    private val mPreviewSizes = SizeMap()

    private val mPictureSizes = SizeMap()

    private var mFacing: Int = 0

    private var mAspectRatio = Constants.DEFAULT_ASPECT_RATIO

    private var mAutoFocus: Boolean = false

    override// Revert
    var flash: Int = 0
        set(flash) {
            if (this.flash == flash) {
                return
            }
            val saved = this.flash
            field = flash
            if (mPreviewRequestBuilder != null) {
                updateFlash()
                if (mCaptureSession != null) {
                    try {
                        mCaptureSession!!.setRepeatingRequest(
                            mPreviewRequestBuilder!!.build(),
                            mCaptureCallback, null
                        )
                    } catch (e: CameraAccessException) {
                        field = saved
                    }

                }
            }
        }

    private var mDisplayOrientation: Int = 0

    override val isCameraOpened: Boolean
        get() = mCamera != null

    override var facing: Int
        get() = mFacing
        set(facing) {
            if (mFacing == facing) {
                return
            }
            mFacing = facing
            if (isCameraOpened) {
                stop()
                start()
            }
        }

    override val supportedAspectRatios: Set<AspectRatio>
        get() = mPreviewSizes.ratios()

    override// TODO: Better error handling
    var aspectRatio: AspectRatio
        get() = mAspectRatio
        set(ratio) {
            if (ratio == null || ratio == mAspectRatio ||
                !mPreviewSizes.ratios().contains(ratio)
            ) {
                return
            }
            mAspectRatio = ratio
            if (mCaptureSession != null) {
                mCaptureSession!!.close()
                mCaptureSession = null
                startCaptureSession()
            }
        }

    override// Revert
    var autoFocus: Boolean
        get() = mAutoFocus
        set(autoFocus) {
            if (mAutoFocus == autoFocus) {
                return
            }
            mAutoFocus = autoFocus
            if (mPreviewRequestBuilder != null) {
                updateAutoFocus()
                if (mCaptureSession != null) {
                    try {
                        mCaptureSession!!.setRepeatingRequest(
                            mPreviewRequestBuilder!!.build(),
                            mCaptureCallback, null
                        )
                    } catch (e: CameraAccessException) {
                        mAutoFocus = !mAutoFocus
                    }

                }
            }
        }

    init {
        mPreview.setCallback(object : PreviewImpl.Callback {
            override fun onSurfaceChanged() {
                startCaptureSession()
            }
        })
    }

    override fun start() {
        chooseCameraIdByFacing()
        collectCameraInfo()
        prepareImageReader()
        startOpeningCamera()
    }

    override fun stop() {
        if (mCaptureSession != null) {
            mCaptureSession!!.close()
            mCaptureSession = null
        }
        if (mCamera != null) {
            mCamera!!.close()
            mCamera = null
        }
        if (mImageReader != null) {
            mImageReader!!.close()
            mImageReader = null
        }
    }

    override fun takePicture() {
        if (mAutoFocus) {
            lockFocus()
        } else {
            captureStillPicture()
        }
    }

    override fun setDisplayOrientation(displayOrientation: Int) {
        mDisplayOrientation = displayOrientation
        mPreview.setDisplayOrientation(mDisplayOrientation)
    }

    /**
     *
     * Chooses a camera ID by the specified camera facing ([.mFacing]).
     *
     * This rewrites [.mCameraId], [.mCameraCharacteristics], and optionally
     * [.mFacing].
     */
    private fun chooseCameraIdByFacing() {
        try {
            val internalFacing = INTERNAL_FACINGS.get(mFacing)
            val ids = mCameraManager.cameraIdList
            for (id in ids) {
                val characteristics = mCameraManager.getCameraCharacteristics(id)
                val internal = characteristics.get(CameraCharacteristics.LENS_FACING)
                    ?: throw NullPointerException("Unexpected state: LENS_FACING null")
                if (internal == internalFacing) {
                    mCameraId = id
                    mCameraCharacteristics = characteristics
                    return
                }
            }
            // Not found
            mCameraId = ids[0]
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId!!)
            val internal = mCameraCharacteristics!!.get(CameraCharacteristics.LENS_FACING)
                ?: throw NullPointerException("Unexpected state: LENS_FACING null")
            var i = 0
            val count = INTERNAL_FACINGS.size()
            while (i < count) {
                if (INTERNAL_FACINGS.valueAt(i) == internal) {
                    mFacing = INTERNAL_FACINGS.keyAt(i)
                    return
                }
                i++
            }
            // The operation can reach here when the only camera device is an external one.
            // We treat it as facing back.
            mFacing = Constants.FACING_BACK
        } catch (e: CameraAccessException) {
            throw RuntimeException("Failed to get a list of camera devices", e)
        }

    }

    /**
     *
     * Collects some information from [.mCameraCharacteristics].
     *
     * This rewrites [.mPreviewSizes], [.mPictureSizes], and optionally,
     * [.mAspectRatio].
     */
    private fun collectCameraInfo() {
        val map = mCameraCharacteristics?.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        ) ?: throw IllegalStateException("Failed to get configuration map: " + mCameraId!!)
        mPreviewSizes.clear()
        for (size in map.getOutputSizes(mPreview.outputClass)) {
            mPreviewSizes.add(Size(size.width, size.height))
        }
        mPictureSizes.clear()
        collectPictureSizes(mPictureSizes, map)

        if (!mPreviewSizes.ratios().contains(mAspectRatio)) {
            mAspectRatio = mPreviewSizes.ratios().iterator().next()
        }
    }

    protected fun collectPictureSizes(sizes: SizeMap, map: StreamConfigurationMap) {
        val outputSizes = map.getHighResolutionOutputSizes(ImageFormat.JPEG)
        if (outputSizes != null) {
            for (size in map.getHighResolutionOutputSizes(ImageFormat.JPEG)) {
                sizes.add(Size(size.width, size.height))
            }
        }
        if (sizes.isEmpty) {
            for (size in map.getOutputSizes(ImageFormat.JPEG)) {
                mPictureSizes.add(Size(size.width, size.height))
            }
        }
    }

    private fun prepareImageReader() {
        val largest = mPictureSizes.sizes(mAspectRatio).last()
        mImageReader = ImageReader.newInstance(
            largest.width, largest.height,
            ImageFormat.JPEG, /* maxImages */ 2
        )
        mImageReader!!.setOnImageAvailableListener(mOnImageAvailableListener, null)
    }

    /**
     *
     * Starts opening a camera device.
     *
     * The result will be processed in [.mCameraDeviceCallback].
     */
    private fun startOpeningCamera() {
        try {
            mCameraManager.openCamera(mCameraId!!, mCameraDeviceCallback, null)
        } catch (e: CameraAccessException) {
            throw RuntimeException("Failed to open camera: " + mCameraId!!, e)
        }

    }

    /**
     *
     * Starts a capture session for camera preview.
     *
     * This rewrites [.mPreviewRequestBuilder].
     *
     * The result will be continuously processed in [.mSessionCallback].
     */
    private fun startCaptureSession() {
        if (!isCameraOpened || !mPreview.isReady || mImageReader == null) {
            return
        }
        val previewSize = chooseOptimalSize()
        mPreview.setBufferSize(previewSize.width, previewSize.height)
        val surface = mPreview.surface
        try {
            mPreviewRequestBuilder = mCamera?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mPreviewRequestBuilder?.addTarget(surface)
            mCamera?.createCaptureSession(
                listOf(surface, mImageReader?.surface),
                mSessionCallback, null
            )
        } catch (e: CameraAccessException) {
            throw RuntimeException("Failed to start camera session")
        }

    }

    /**
     * Chooses the optimal preview size based on [.mPreviewSizes] and the surface size.
     *
     * @return The picked size for camera preview.
     */
    private fun chooseOptimalSize(): Size {
        val surfaceLonger: Int
        val surfaceShorter: Int
        val surfaceWidth = mPreview.width
        val surfaceHeight = mPreview.height
        if (surfaceWidth < surfaceHeight) {
            surfaceLonger = surfaceHeight
            surfaceShorter = surfaceWidth
        } else {
            surfaceLonger = surfaceWidth
            surfaceShorter = surfaceHeight
        }
        val candidates = mPreviewSizes.sizes(mAspectRatio)
        // Pick the smallest of those big enough.
        for (size in candidates) {
            if (size.width >= surfaceLonger && size.height >= surfaceShorter) {
                return size
            }
        }
        // If no size is big enough, pick the largest one.
        return candidates.last()
    }

    /**
     * Updates the internal state of auto-focus to [.mAutoFocus].
     */
    private fun updateAutoFocus() {
        if (mAutoFocus) {
            val modes = mCameraCharacteristics!!.get(
                CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES
            )
            // Auto focus is not supported
            if (modes == null || modes!!.size == 0 ||
                modes!!.size == 1 && modes!![0] == CameraCharacteristics.CONTROL_AF_MODE_OFF
            ) {
                mAutoFocus = false
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_OFF
                )
            } else {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }
        } else {
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF
            )
        }
    }

    /**
     * Updates the internal state of flash to [.mFlash].
     */
    private fun updateFlash() {
        when (flash) {
            Constants.FLASH_OFF -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
            Constants.FLASH_ON -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
            Constants.FLASH_TORCH -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_TORCH
                )
            }
            Constants.FLASH_AUTO -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
            Constants.FLASH_RED_EYE -> {
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE
                )
                mPreviewRequestBuilder!!.set(
                    CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF
                )
            }
        }
    }

    /**
     * Locks the focus as the first step for a still image capture.
     */
    private fun lockFocus() {
        mPreviewRequestBuilder!!.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CaptureRequest.CONTROL_AF_TRIGGER_START
        )
        try {
            mCaptureCallback.setState(PictureCaptureCallback.STATE_LOCKING)
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback, null)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to lock focus.", e)
        }

    }

    /**
     * Captures a still picture.
     */
    private fun captureStillPicture() {
        try {
            val captureRequestBuilder = mCamera!!.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE
            )
            captureRequestBuilder.addTarget(mImageReader!!.getSurface())
            captureRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                mPreviewRequestBuilder!!.get(CaptureRequest.CONTROL_AF_MODE)
            )
            when (flash) {
                Constants.FLASH_OFF -> {
                    captureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON
                    )
                    captureRequestBuilder.set(
                        CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_OFF
                    )
                }
                Constants.FLASH_ON -> captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH
                )
                Constants.FLASH_TORCH -> {
                    captureRequestBuilder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON
                    )
                    captureRequestBuilder.set(
                        CaptureRequest.FLASH_MODE,
                        CaptureRequest.FLASH_MODE_TORCH
                    )
                }
                Constants.FLASH_AUTO -> captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                Constants.FLASH_RED_EYE -> captureRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
            }
            // Calculate JPEG orientation.
            val sensorOrientation = mCameraCharacteristics!!.get(
                CameraCharacteristics.SENSOR_ORIENTATION
            )!!
            captureRequestBuilder.set(
                CaptureRequest.JPEG_ORIENTATION,
                (sensorOrientation!! +
                        mDisplayOrientation * (if (mFacing == Constants.FACING_FRONT) 1 else -1) +
                        360) % 360
            )
            // Stop preview and capture a still picture.
            mCaptureSession!!.stopRepeating()
            mCaptureSession!!.capture(captureRequestBuilder.build(),
                object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        unlockFocus()
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Cannot capture a still picture.", e)
        }

    }

    /**
     * Unlocks the auto-focus and restart camera preview. This is supposed to be called after
     * capturing a still picture.
     */
    private fun unlockFocus() {
        mPreviewRequestBuilder!!.set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CaptureRequest.CONTROL_AF_TRIGGER_CANCEL
        )
        try {
            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback, null)
            updateAutoFocus()
            updateFlash()
            mPreviewRequestBuilder!!.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_IDLE
            )
            mCaptureSession!!.setRepeatingRequest(mPreviewRequestBuilder!!.build(), mCaptureCallback, null)
            mCaptureCallback.setState(PictureCaptureCallback.STATE_PREVIEW)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to restart camera preview.", e)
        }

    }

    /**
     * A [CameraCaptureSession.CaptureCallback] for capturing a still picture.
     */
    private abstract class PictureCaptureCallback internal constructor() : CameraCaptureSession.CaptureCallback() {

        private var mState: Int = 0

        internal fun setState(state: Int) {
            mState = state
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            process(result)
        }

        private fun process(result: CaptureResult) {
            when (mState) {
                STATE_LOCKING -> {
                    val af = result.get(CaptureResult.CONTROL_AF_STATE) ?: return
                    if ((af == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED || af == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED)) {
                        val ae = result.get(CaptureResult.CONTROL_AE_STATE)
                        if (ae == null || ae == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            setState(STATE_CAPTURING)
                            onReady()
                        } else {
                            setState(STATE_LOCKED)
                            onPrecaptureRequired()
                        }
                    }
                }
                STATE_PRECAPTURE -> {
                    val ae = result.get(CaptureResult.CONTROL_AE_STATE)
                    if ((ae == null || ae == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                                ae == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED)
                    ) {
                        setState(STATE_WAITING)
                    }
                }
                STATE_WAITING -> {
                    val ae = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (ae == null || ae != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        setState(STATE_CAPTURING)
                        onReady()
                    }
                }
            }
        }

        /**
         * Called when it is ready to take a still picture.
         */
        abstract fun onReady()

        /**
         * Called when it is necessary to run the precapture sequence.
         */
        abstract fun onPrecaptureRequired()

        companion object {

            internal val STATE_PREVIEW = 0
            internal val STATE_LOCKING = 1
            internal val STATE_LOCKED = 2
            internal val STATE_PRECAPTURE = 3
            internal val STATE_WAITING = 4
            internal val STATE_CAPTURING = 5
        }

    }

    companion object {

        private val TAG = "Camera2"

        private val INTERNAL_FACINGS = SparseIntArray()

        init {
            INTERNAL_FACINGS.put(Constants.FACING_BACK, CameraCharacteristics.LENS_FACING_BACK)
            INTERNAL_FACINGS.put(Constants.FACING_FRONT, CameraCharacteristics.LENS_FACING_FRONT)
        }
    }

}