package com.appknot.module.widget.canvas

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.Toast


/**
 *
 * @author Jin on 2019-10-07
 */

class OnPinchListener(val context: Context, val imageView: ImageView, val bitmap: Bitmap)
    : ScaleGestureDetector.SimpleOnScaleGestureListener() {

    companion object {
        const val TAG_PINCH_LISTENER = "PINCH_LISTENER"
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (detector != null) {

            val scaleFactor = detector.scaleFactor

            if (imageView != null) {

                // Scale the image with pinch zoom value.
                scaleView(scaleFactor, scaleFactor)

            } else {
                if (context != null) {
                    Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e(TAG_PINCH_LISTENER, "Both context and srcImageView is null.")
                }
            }
        } else {
            Log.e(TAG_PINCH_LISTENER, "Pinch listener onScale detector parameter is null.")
        }

        return true
    }

    private fun scaleView(xScale: Float, yScale: Float) {
        val width = bitmap.width
        val height = bitmap.height
        val config = bitmap.config

        val scaleBitmap =
            Bitmap.createBitmap((width * xScale).toInt(), (height * yScale).toInt(), config)
        val scaleCanvas = Canvas(scaleBitmap)
        val scaleMatrix = Matrix()

        scaleMatrix.setScale(xScale, yScale)

        val paint = Paint()

        scaleCanvas.drawBitmap(bitmap, scaleMatrix, paint)

        imageView.setImageBitmap(scaleBitmap)
    }
}