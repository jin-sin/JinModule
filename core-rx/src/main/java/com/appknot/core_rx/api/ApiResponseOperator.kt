package com.appknot.core_rx.api

abstract class ApiResponseOperator<T> {
    abstract fun onSuccess(apiResponse: ApiResponse.Success<T>)

    abstract fun onError(apiResponse: ApiResponse.Failure.Error<T>)

    abstract fun onException(apiResponse: ApiResponse.Failure.Exception<T>)
}