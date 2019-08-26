package com.appknot.module.widget.camera

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.core.os.ParcelableCompat
import androidx.core.os.ParcelableCompatCreatorCallbacks
import androidx.core.view.ViewCompat
import com.appknot.module.R
import com.appknot.module.widget.camera.base.*
import java.lang.annotation.RetentionPolicy


/**
 *
 * @author Jin on 2019-08-23
 */
class AKCameraView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    internal val mImpl: CameraViewImpl

    private val mCallbacks: CallbackBridge

    private var mAdjustViewBounds: Boolean = false

    private val mDisplayOrientationDetector: DisplayOrientationDetector

    /**
     * @return `true` if the camera is opened.
     */
    val isCameraOpened: Boolean
        get() = mImpl.isCameraOpened

    /**
     * @return True when this CameraView is adjusting its bounds to preserve the aspect ratio of
     * camera.
     * @see .setAdjustViewBounds
     */
    /**
     * @param adjustViewBounds `true` if you want the CameraView to adjust its bounds to
     * preserve the aspect ratio of camera.
     * @see .getAdjustViewBounds
     */
    var adjustViewBounds: Boolean
        get() = mAdjustViewBounds
        set(adjustViewBounds) {
            if (mAdjustViewBounds != adjustViewBounds) {
                mAdjustViewBounds = adjustViewBounds
                requestLayout()
            }
        }

    /**
     * Gets the direction that the current camera faces.
     *
     * @return The camera facing.
     */
    /**
     * Chooses camera by the direction it faces.
     *
     * @param facing The camera facing. Must be either [.FACING_BACK] or
     * [.FACING_FRONT].
     */
    var facing: Int
        get() = mImpl.facing
        set(facing) {
            mImpl.facing = facing
        }

    /**
     * Gets all the aspect ratios supported by the current camera.
     */
    val supportedAspectRatios: Set<AspectRatio>
        get() = mImpl.supportedAspectRatios

    /**
     * Gets the current aspect ratio of camera.
     *
     * @return The current [AspectRatio]. Can be `null` if no camera is opened yet.
     */
    /**
     * Sets the aspect ratio of camera.
     *
     * @param ratio The [AspectRatio] to be set.
     */
    var aspectRatio: AspectRatio
        get() = mImpl.aspectRatio
        set(ratio) {
            mImpl.aspectRatio = ratio
        }

    /**
     * Returns whether the continuous auto-focus mode is enabled.
     *
     * @return `true` if the continuous auto-focus mode is enabled. `false` if it is
     * disabled, or if it is not supported by the current camera.
     */
    /**
     * Enables or disables the continuous auto-focus mode. When the current camera doesn't support
     * auto-focus, calling this method will be ignored.
     *
     * @param autoFocus `true` to enable continuous auto-focus mode. `false` to
     * disable it.
     */
    var autoFocus: Boolean
        get() = mImpl.autoFocus
        set(autoFocus) {
            mImpl.autoFocus = autoFocus
        }

    /**
     * Gets the current flash mode.
     *
     * @return The current flash mode.
     */
    /**
     * Sets the flash mode.
     *
     * @param flash The desired flash mode.
     */
    var flash: Int
        get() = mImpl.flash
        set(flash) {
            mImpl.flash = flash
        }


    init {
        // Internal setup
        val preview: PreviewImpl
        preview = TextureViewPreview(context, this)
        mCallbacks = CallbackBridge()
        mImpl = if (Build.VERSION.SDK_INT < 23) {
            AKCamera(mCallbacks, preview, context)
        } else {
            AKCamera(mCallbacks, preview, context)
        }
        // Attributes
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.CameraView, defStyleAttr,
            R.style.Widget_CameraView
        )
        mAdjustViewBounds = a.getBoolean(R.styleable.CameraView_android_adjustViewBounds, false)
        facing = a.getInt(R.styleable.CameraView_facing, FACING_BACK)
        var aspectRatio = a.getString(R.styleable.CameraView_aspectRatio)
        if (aspectRatio != null) {
            this@AKCameraView.aspectRatio = AspectRatio.parse(aspectRatio)
        } else {
            this@AKCameraView.aspectRatio = Constants.DEFAULT_ASPECT_RATIO
        }
        autoFocus = a.getBoolean(R.styleable.CameraView_autoFocus, true)
        flash = a.getInt(R.styleable.CameraView_flash, Constants.FLASH_AUTO)
        a.recycle()
        // Display orientation detector
        mDisplayOrientationDetector = object : DisplayOrientationDetector(context) {
            override fun onDisplayOrientationChanged(displayOrientation: Int) {
                mImpl.setDisplayOrientation(displayOrientation)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mDisplayOrientationDetector.enable(ViewCompat.getDisplay(this)!!)
    }

    override fun onDetachedFromWindow() {
        mDisplayOrientationDetector.disable()
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Handle android:adjustViewBounds
        if (mAdjustViewBounds) {
            if (!isCameraOpened) {
                mCallbacks.reserveRequestLayoutOnOpen()
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                return
            }
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
                val ratio = aspectRatio
                var height = (MeasureSpec.getSize(widthMeasureSpec) * ratio.toFloat()).toInt()
                if (heightMode == MeasureSpec.AT_MOST) {
                    height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec))
                }
                super.onMeasure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                )
            } else if (widthMode != MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
                val ratio = aspectRatio
                var width = (MeasureSpec.getSize(heightMeasureSpec) * ratio.toFloat()).toInt()
                if (widthMode == MeasureSpec.AT_MOST) {
                    width = Math.min(width, MeasureSpec.getSize(widthMeasureSpec))
                }
                super.onMeasure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    heightMeasureSpec
                )
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
        // Measure the TextureView
        val width = measuredWidth
        val height = measuredHeight
        var ratio: AspectRatio? = aspectRatio
        if (mDisplayOrientationDetector.lastKnownDisplayOrientation % 180 === 0) {
            ratio = ratio!!.inverse()
        }
        assert(ratio != null)
        if (height < width * ratio!!.y / ratio.x) {
            mImpl.view.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(
                    width * ratio.y / ratio.x,
                    MeasureSpec.EXACTLY
                )
            )
        } else {
            mImpl.view.measure(
                MeasureSpec.makeMeasureSpec(
                    height * ratio.x / ratio.y,
                    MeasureSpec.EXACTLY
                ),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val state = SavedState(super.onSaveInstanceState()!!)
        state.facing = facing
        state.ratio = aspectRatio
        state.autoFocus = autoFocus
        state.flash = flash
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.getSuperState())
        facing = ss.facing
        aspectRatio = ss.ratio
        autoFocus = ss.autoFocus
        flash = ss.flash
    }

    /**
     * Open a camera device and start showing camera preview. This is typically called from
     * [Activity.onResume].
     */
    fun start() {
        mImpl.start()
    }

    /**
     * Stop camera preview and close the device. This is typically called from
     * [Activity.onPause].
     */
    fun stop() {
        mImpl.stop()
    }

    /**
     * Add a new callback.
     *
     * @param callback The [Callback] to addWonderple.
     * @see .removeCallback
     */
    fun addCallback(callback: Callback) {
        mCallbacks.add(callback)
    }

    /**
     * Remove a callback.
     *
     * @param callback The [Callback] to remove.
     * @see .addCallback
     */
    fun removeCallback(callback: Callback) {
        mCallbacks.remove(callback)
    }

    /**
     * Take a picture. The result will be returned to
     * [Callback.onPictureTaken].
     */
    fun takePicture() {
        mImpl.takePicture()
    }

    private inner class CallbackBridge internal constructor() : CameraViewImpl.Callback {

        private val mCallbacks = ArrayList<Callback>()

        private var mRequestLayoutOnOpen: Boolean = false

        fun add(callback: Callback) {
            mCallbacks.add(callback)
        }

        fun remove(callback: Callback) {
            mCallbacks.remove(callback)
        }

        override fun onCameraOpened() {
            if (mRequestLayoutOnOpen) {
                mRequestLayoutOnOpen = false
                requestLayout()
            }
            for (callback in mCallbacks) {
                callback.onCameraOpened(this@AKCameraView)
            }
        }

        override fun onCameraClosed() {
            for (callback in mCallbacks) {
                callback.onCameraClosed(this@AKCameraView)
            }
        }

        override fun onPictureTaken(data: ByteArray) {
            for (callback in mCallbacks) {
                callback.onPictureTaken(this@AKCameraView, data)
            }
        }

        internal fun reserveRequestLayoutOnOpen() {
            mRequestLayoutOnOpen = true
        }
    }

    private class SavedState : BaseSavedState {

        internal var facing: Int = 0

        internal lateinit var ratio: AspectRatio

        internal var autoFocus: Boolean = false

        internal var flash: Int = 0

        internal constructor(source: Parcel, loader: ClassLoader) : super(source) {
            facing = source.readInt()
            ratio = source.readParcelable(loader)!!
            autoFocus = source.readByte().toInt() != 0
            flash = source.readInt()
        }

        internal constructor(superState: Parcelable) : super(superState) {}

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(facing)
            out.writeParcelable(ratio, 0)
            out.writeByte((if (autoFocus) 1 else 0).toByte())
            out.writeInt(flash)
        }

        companion object {

            @SuppressLint("ParcelCreator")
            val CREATOR: Parcelable.Creator<SavedState> =
                ParcelableCompat.newCreator(object : ParcelableCompatCreatorCallbacks<SavedState> {

                    override fun createFromParcel(`in`: Parcel, loader: ClassLoader): SavedState {
                        return SavedState(`in`, loader)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }

                })
        }

    }

    /**
     * Callback for monitoring events about [CameraView].
     */
    abstract class Callback {

        /**
         * Called when camera is opened.
         *
         * @param cameraView The associated [CameraView].
         */
        open fun onCameraOpened(cameraView: AKCameraView) {}

        /**
         * Called when camera is closed.
         *
         * @param cameraView The associated [CameraView].
         */
        open fun onCameraClosed(cameraView: AKCameraView) {}

        /**
         * Called when a picture is taken.
         *
         * @param cameraView The associated [CameraView].
         * @param data       JPEG data.
         */
        open fun onPictureTaken(cameraView: AKCameraView, data: ByteArray) {}
    }

    companion object {

        /**
         * The camera device faces the opposite direction as the device's screen.
         */
        val FACING_BACK = Constants.FACING_BACK

        /**
         * The camera device faces the same direction as the device's screen.
         */
        val FACING_FRONT = Constants.FACING_FRONT

        /**
         * Flash will not be fired.
         */
        val FLASH_OFF = Constants.FLASH_OFF

        /**
         * Flash will always be fired during snapshot.
         */
        val FLASH_ON = Constants.FLASH_ON

        /**
         * Constant emission of light during preview, auto-focus and snapshot.
         */
        val FLASH_TORCH = Constants.FLASH_TORCH

        /**
         * Flash will be fired automatically when required.
         */
        val FLASH_AUTO = Constants.FLASH_AUTO

        /**
         * Flash will be fired in red-eye reduction mode.
         */
        val FLASH_RED_EYE = Constants.FLASH_RED_EYE
    }

}