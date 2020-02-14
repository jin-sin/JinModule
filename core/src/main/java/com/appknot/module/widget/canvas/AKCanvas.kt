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
import android.util.Log


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
    var isZoom = false
    private var posX = 0F
    private var posY = 0F
    private var lastTouchX = 0F
    private var lastTouchY = 0F
    private val INVALID_POINTER_ID = -1
    private var activePointerId = INVALID_POINTER_ID

    var onLayoutListener: ((Canvas) -> Unit)? = null
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
//        if (isZoom) canvas.translate(posX, posY)

        canvas.drawPath(path, paint)
        this.canvas?.drawPath(path, paint)

        canvas.restore()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

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
            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_POINTER_2_DOWN -> {
                isZoom = true
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)

                if (!isZoom) {
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    posX += dx
                    posY += dy

                    path.lineTo(x, y)

                    invalidate()
                }

                lastTouchX = x
                lastTouchY = y


//                if (!isZoom) {
////                    scaleGestureDetector.onTouchEvent(event)
//                }
            }
            MotionEvent.ACTION_UP -> {
                isZoom = false
                activePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_CANCEL -> {
                isZoom = false
                activePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                isZoom = false

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

        Log.d("AKCanvas onTouch isZoom", isZoom.toString())

        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap?.let {
            canvas = Canvas(it)
            draw(canvas)

            canvas?.let { canvas -> onLayoutListener?.invoke(canvas) }
        }
    }

}