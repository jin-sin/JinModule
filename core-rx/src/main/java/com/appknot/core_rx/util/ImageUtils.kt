package com.appknot.core_rx.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.appknot.core_rx.R
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.ViewTarget

/**
 *
 * @author Jin on 2020-02-21
 */

fun ImageView.loadImage(url: String)    {
    GlideApp.with(this.context)
        .load(url)
        .into(this)
}

fun ImageView.loadImageWithPlaceHolder(url: String, resId: Int) {
    GlideApp.with(this.context)
        .load(url)
        .placeholder(resId)
        .into(this)
}

fun ImageView.loadImageWithPlaceHolder(url: String, drawable: Drawable) {
    GlideApp.with(this.context)
        .load(url)
        .placeholder(drawable)
        .into(this)
}

fun ImageView.loadImageToBitmap(url: String)    {
    GlideApp.with(this.context)
        .asBitmap()
        .load(url)
        .listener(object : RequestListener<Bitmap>  {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                target: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                this@loadImageToBitmap.setImageBitmap(resource)
                return false
            }
        }).submit()
}

@GlideModule
class GlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        ViewTarget.setTagId(R.string.app_name)
        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
    }
}