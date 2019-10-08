package com.appknot.module.widget.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import android.R.attr.action



/**
 *
 * @author Jin on 2019-09-24
 */

class AKCanvas : AppCompatImageView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    val paint = Paint()

    val path = Path()
    lateinit var bitmap: Bitmap
    private var scaledBitmap: Bitmap? = null
    var canvas: Canvas? = null

    lateinit var scaleGestureDetector: ScaleGestureDetector
    var scaleFactor: Float = 1F
    private var posX = 0F
    private var posY = 0F
    private var lastTouchX = 0F
    private var lastTouchY = 0F
    private val INVALID_POINTER_ID = -1
    private var activePointerId = INVALID_POINTER_ID

    init {

        paint.color = Color.RED
        paint.strokeWidth = 30F
        paint.style = Paint.Style.STROKE

        scaleType = ScaleType.CENTER_INSIDE
    }

    fun getScaledBitmap(width: Int, height: Int): Bitmap? =
        bitmap?.let { Bitmap.createScaledBitmap(it, width, height, false) }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
//        canvas.translate(posX, posY)
        canvas.scale(scaleFactor, scaleFactor)

        canvas.drawPath(path, paint)
        this.canvas?.drawPath(path, paint)

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        val x = event.x
        val y = event.y
        val pointerCount = event.pointerCount

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = x
                lastTouchY = y

                activePointerId = event.getPointerId(0)

                path.moveTo(x, y)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex =
                    action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT

            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)

                if (!scaleGestureDetector.isInProgress) {
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    posX += dx
                    posY += dy

                    invalidate()
                }

                lastTouchX = x
                lastTouchY = y

                path.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
                activePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex =
                    action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerId == 0) 1 else 0
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }

//        invalidate()


        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap?.let {
            canvas = Canvas(it)
            draw(canvas)

            scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        }
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {

            scaleFactor *= detector.scaleFactor
            scaleFactor = Math.max(0.1F, Math.min(scaleFactor, 5.0F))

            scaleX = scaleFactor
            scaleY = scaleFactor

//            val scaleBitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.width * detector.scaleFactor).toInt(), (bitmap.height * detector.scaleFactor).toInt(), false)
//            val scaleCanvas = Canvas(scaleBitmap)
//            val scaleMatrix = Matrix()
//
//            scaleMatrix.setScale(detector.scaleFactor, detector.scaleFactor)
//            scaleCanvas.drawBitmap(scaleBitmap, scaleMatrix, paint)

            invalidate()

            return true
        }
    }
}