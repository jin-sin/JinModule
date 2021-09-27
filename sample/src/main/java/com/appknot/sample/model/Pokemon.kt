package com.appknot.sample.model

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 *
 * @author Jin on 2021/09/27
 */

@Parcelize
@JsonClass(generateAdapter = true)
data class Pokemon(
    var page: Int = 0,
    @field:Json(name = "name") val name: String,
    @field:Json(name = "url") val url: String
) : Parcelable {

    fun getImageUrl(): String {
        val index = url.split("/".toRegex()).dropLast(1).last()
        return "https://pokeres.bastionbot.org/images/pokemon/$index.png"
    }
}