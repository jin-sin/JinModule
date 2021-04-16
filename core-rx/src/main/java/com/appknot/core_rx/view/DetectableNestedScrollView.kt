package com.appknot.core_rx.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import java.lang.NullPointerException

/**
 *
 * @author Jin on 4/16/21
 *
 * usage :
 * detectableNestedScrollView.linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
 * detectableNestedScrollView.isBottomOfList = IsBottomOfList
 */
class DetectableNestedScrollView : NestedScrollView {
    var SCREEN_HEIGHT = 0
    var isBottomOfList: IsBottomOfList? = null
    var linearLayoutManager: LinearLayoutManager? = null
    private val TAG = "DetectingNestedScrollView"


    private val batchSize = 20L
    private var currentPage: Long = 0L
    private val threshold = 10
    private var endWithAuto = false
    val startSize: Long
        get() = ++currentPage

    val maxSize: Long
        get() = currentPage + batchSize

    constructor(@NonNull context: Context) : super(context) {
        init()
    }

    constructor(@NonNull context: Context, @Nullable attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    constructor(
        @NonNull context: Context,
        @Nullable attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        SCREEN_HEIGHT = context.resources.displayMetrics.heightPixels
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (isBottomOfList != null) {
            if (isBottomOfList!!.isLastPage) return

            endWithAuto = if (isVisible() && !endWithAuto) {
                isBottomOfList!!.loadMore(startSize, maxSize)
                true
            } else {
                false
            }
        }
    }

    fun isVisible(): Boolean {
        var view: View?
        val childCount: Int
        if (linearLayoutManager != null) {
            childCount = linearLayoutManager!!.childCount
            view = linearLayoutManager!!.getChildAt(childCount - 1)
        } else {
            throw NullPointerException("layoutManager must not be null")
        }
        if (view == null) {
            throw NullPointerException("view must not be null")
            return false
        }
        if (!view.isShown) {
            Log.v(TAG, "!view.isShown()")
            return false
        }
        val actualPosition = Rect()
        view.getGlobalVisibleRect(actualPosition)
        val height1: Int = view.height
        val height2: Int = actualPosition.bottom - actualPosition.top
//        Log.v(
//            TAG,
//            "actualPosition.bottom = " + actualPosition.bottom.toString() + "/ HomePage.SCREEN_HEIGHT =" +
//                    SCREEN_HEIGHT.toString() + " / height1 = " + height1.toString() + "/ height2 = " + height2
//        )

//        endWithAuto = actualPosition.bottom >= SCREEN_HEIGHT * (batchSize / threshold)

        return  actualPosition.bottom < SCREEN_HEIGHT && !endWithAuto
    }

    interface IsBottomOfList {
        var isLastPage: Boolean
        fun loadMore(start: Long, count: Long)
    }
}