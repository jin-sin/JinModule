package com.appknot.module.image

import android.content.Context
import com.appknot.module.R
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ViewTarget


/**
 * Glide API 를 제너레이트 하기 위해 필요한 클래스
 * @author Xellsky(Alex Ji)
 */
@GlideModule
class GlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        ViewTarget.setTagId(R.string.app_name)
        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
    }
}