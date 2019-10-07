package com.appknot.module.widget.canvas

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

/**
 *
 * @author Jin on 2019-09-24
 */

class AKCanvas : View {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    val paint = Paint()

    val path = Path()
    lateinit var bitmap: Bitmap
    private var scaledBitmap: Bitmap? = null
    var canvas: Canvas? = null

    val scaleGestureDetector = ScaleGestureDetector(context, OnPinchListener(context, this, bitmap))

    init {

        paint.color = Color.RED
        paint.strokeWidth = 30F
        paint.style = Paint.Style.STROKE
    }

    fun getScaledBitmap(width: Int, height: Int): Bitmap? =
        bitmap?.let { Bitmap.createScaledBitmap(it, width, height, false) }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
        this.canvas?.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
            }
            MotionEvent.ACTION_UP -> {
            }
        }

        invalidate()

        scaleGestureDetector.onTouchEvent(event)

        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap?.let {
            canvas = Canvas(it)
            draw(canvas)
        }
    }
}