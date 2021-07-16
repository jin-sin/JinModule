package com.appknot.core_rx.binding

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.appknot.core_rx.util.GlideApp

object ViewBinding {

    @JvmStatic
    @BindingAdapter("gone")
    fun bindGone(view: View, shouldBeGone: Boolean) {
        view.visibility = if (shouldBeGone) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    @JvmStatic
    @BindingAdapter("setImage")
    fun bindImage(view: ImageView, url: String) {
        GlideApp.with(view)
            .load(url)
            .load(view)
    }
}