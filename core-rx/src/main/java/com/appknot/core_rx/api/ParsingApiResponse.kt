package com.appknot.core_rx.api

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize
import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.Serializable


/**
 * appknot 컨벤션 기반 API 응답 구조
 * @author Jin Sin
 */

@JsonClass(generateAdapter = true)
open class ParsingApiResponse<T> : Serializable {
    lateinit var code: String
    lateinit var msg: Msg
    var data: T? = null

    class Msg : Serializable {
        lateinit var ko: String
        lateinit var en: String
    }
}