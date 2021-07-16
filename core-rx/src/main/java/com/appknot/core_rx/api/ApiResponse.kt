package com.appknot.core_rx.api

import okhttp3.Headers
import okhttp3.ResponseBody
import retrofit2.Response


/**
 * appknot 컨벤션 기반 API 응답 구조
 * @author Xellsky(Alex Ji)
 */
open class ApiResponse<T> {
    lateinit var code: String
    lateinit var msg: Msg
    var data: Any? = null

    inner class Msg {
        lateinit var ko: String
        lateinit var en: String
    }

    data class Success<T>(val response: Response<T>) : ApiResponse<T>() {
        val statusCode: StatusCode = getStatusCodeFromResponse(response)
        val headers: Headers = response.headers()
        val raw: okhttp3.Response = response.raw()
        val body: T by lazy { response.body() ?: throw NoContentException(statusCode.code) }
        override fun toString() = "[ApiResponse.Success](data=$data)"
    }


    sealed class Failure<T> {
        data class Error<T>(val response: Response<T>) : ApiResponse<T>() {
//            lateinit var code: String
            val headers: Headers = response.headers()
            val raw: okhttp3.Response = response.raw()
            val errorBody: ResponseBody? = response.errorBody()
            override fun toString(): String = "[ApiResponse.Failure.Error-$](errorResponse=$response)"
        }

        data class Exception<T>(val exception: Throwable) : ApiResponse<T>() {
            val message: String? = exception.localizedMessage
            override fun toString(): String = "[ApiResponse.Failure.Exception](message=$message)"
        }
    }

    fun <T> getStatusCodeFromResponse(response: Response<T>): StatusCode {
        return StatusCode.values().find { it.code == response.code() }
            ?: StatusCode.Unknown
    }


    companion object {
        @JvmSynthetic
        inline fun <T> of(
            successCodeRange: IntRange = 200..299,
            crossinline f: () -> Response<T>
        ): ApiResponse<T> = try {
            val response = f()
            if (response.raw().code in successCodeRange) {
                Success(response)
            } else {
                Failure.Error(response)
            }
        } catch (ex: Exception) {
            Failure.Exception(ex)
        }

        fun <T> error(ex: Throwable) = Failure.Exception<T>(ex)
    }

}