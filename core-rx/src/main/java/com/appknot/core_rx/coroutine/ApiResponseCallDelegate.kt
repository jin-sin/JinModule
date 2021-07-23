package com.appknot.core_rx.coroutine

import com.appknot.core_rx.api.TransApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class ApiResponseCallDelegate<T>(proxy: Call<T>) : CallDelegate<T, TransApiResponse<T>>(proxy) {

    override fun enqueueImpl(callback: Callback<TransApiResponse<T>>) = proxy.enqueue(
        object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val apiResponse = TransApiResponse.of { response }
                callback.onResponse(this@ApiResponseCallDelegate, Response.success(apiResponse))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(this@ApiResponseCallDelegate, Response.success(TransApiResponse.error(t)))
            }
        }
    )

    override fun cloneImpl() = ApiResponseCallDelegate(proxy.clone())
}