package com.appknot.module.widget.camera.base

import android.graphics.SurfaceTexture
import android.annotation.TargetApi
import android.view.TextureView
import android.content.Context
import android.graphics.Matrix
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import com.appknot.module.R


/**
 *
 * @author Jin on 2019-08-23
 */

class TextureViewPreview(context: Context, parent: ViewGroup) : PreviewImpl() {

    private val mTextureView: TextureView

    private var mDisplayOrientation: Int = 0

    override val surface: Surface
        get() = Surface(mTextureView.surfaceTexture)

    override val surfaceTexture: SurfaceTexture?
        get() = mTextureView.surfaceTexture

    override val view: View
        get() = mTextureView

    override val outputClass: Class<*>
        get() = SurfaceTexture::class.java

    override val isReady: Boolean
        get() = mTextureView.surfaceTexture != null

    init {
        val view = View.inflate(context, R.layout.view_texture, parent)
        mTextureView = view.findViewById(R.id.texture_view)
        mTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                setSize(width, height)
                configureTransform()
                dispatchSurfaceChanged()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                setSize(width, height)
                configureTransform()
                dispatchSurfaceChanged()
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                setSize(0, 0)
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    // This method is called only from Camera2.
    @TargetApi(15)
    override fun setBufferSize(width: Int, height: Int) {
        mTextureView.surfaceTexture?.setDefaultBufferSize(width, height)
    }

    override fun setDisplayOrientation(displayOrientation: Int) {
        mDisplayOrientation = displayOrientation
        configureTransform()
    }

    /**
     * Configures the transform matrix for TextureView based on [.mDisplayOrientation] and
     * the surface size.
     */
    fun configureTransform() {
        val matrix = Matrix()
        if (mDisplayOrientation % 180 == 90) {
            val width = width
            val height = height
            // Rotate the camera preview when the screen is landscape.
            matrix.setPolyToPoly(
                floatArrayOf(
                    0f, 0f, // top left
                    width.toFloat(), 0f, // top right
                    0f, height.toFloat(), // bottom left
                    width.toFloat(), height.toFloat()
                )// bottom right
                , 0,
                if (mDisplayOrientation == 90)
                // Clockwise
                    floatArrayOf(
                        0f, height.toFloat(), // top left
                        0f, 0f, // top right
                        width.toFloat(), height.toFloat(), // bottom left
                        width.toFloat(), 0f
                    )// bottom right
                else
                // mDisplayOrientation == 270
                // Counter-clockwise
                    floatArrayOf(
                        width.toFloat(), 0f, // top left
                        width.toFloat(), height.toFloat(), // top right
                        0f, 0f, // bottom left
                        0f, height.toFloat()
                    )// bottom right
                , 0,
                4
            )
        }
        mTextureView.setTransform(matrix)
    }

}