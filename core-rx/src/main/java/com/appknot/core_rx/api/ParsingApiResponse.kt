package com.appknot.core_rx.api

import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response


/**
 * appknot 컨벤션 기반 API 응답 구조
 * @author Jin Sin
 */
abstract class ParsingApiResponse<T> {
    lateinit var code: String
    lateinit var msg: Msg
    abstract var data: T?

    inner class Msg {
        lateinit var ko: String
        lateinit var en: String
    }
}