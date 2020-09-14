package com.appknot.core_rx.extensions

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View

/**
 *
 * @author Jin on 2020/09/14
 */

/**
 *
 * */
fun View.setBackgroundStateSelectSelector(
    unSelectConfig: DrawableConfig,
    selectConfig: DrawableConfig = unSelectConfig
) {
    val stateListDrawable = StateListDrawable()
    val selectDrawable = GradientDrawable()
    val unSelectDrawable = GradientDrawable()

    with(selectConfig) {
        selectDrawable.setStroke(strokeWidth, strokeColor)
        selectDrawable.setColor(solidColor)
        selectDrawable.shape = this.shape
        selectDrawable.cornerRadius = this.cornerRadius
    }

    with(unSelectConfig) {
        unSelectDrawable.setStroke(strokeWidth, strokeColor)
        unSelectDrawable.setColor(solidColor)
        unSelectDrawable.shape = this.shape
        unSelectDrawable.cornerRadius = this.cornerRadius
    }

    stateListDrawable.addState(intArrayOf(android.R.attr.state_selected), selectDrawable)
    stateListDrawable.addState(intArrayOf(-android.R.attr.state_selected), unSelectDrawable)

    this.background = stateListDrawable
}

data class DrawableConfig(
    var strokeWidth: Int,
    var strokeColor: Int,
    var solidColor: Int,
    var cornerRadius: Float,
    var shape: Int
)