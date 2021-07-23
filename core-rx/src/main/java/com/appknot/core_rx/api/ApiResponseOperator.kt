package com.appknot.core_rx.api

abstract class ApiResponseOperator<T> {
    abstract fun onSuccess(apiResponse: TransApiResponse.Success<T>)

    abstract fun onError(apiResponse: TransApiResponse.Failure.Error<T>)

    abstract fun onException(apiResponse: TransApiResponse.Failure.Exception<T>)
}