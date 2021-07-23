package com.appknot.core_rx.coroutine

import com.appknot.core_rx.api.TransApiResponse
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class CoroutinesResponseCallAdapter constructor(
    private val resultType: Type
) : CallAdapter<Type, Call<TransApiResponse<Type>>> {

    override fun responseType(): Type {
        return resultType
    }

    override fun adapt(call: Call<Type>): Call<TransApiResponse<Type>> {
        return ApiResponseCallDelegate(call)
    }
}