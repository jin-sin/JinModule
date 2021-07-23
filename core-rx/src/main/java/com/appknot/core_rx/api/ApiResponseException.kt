package com.appknot.core_rx.api

import java.lang.RuntimeException

/**
 *
 * @author Jin on 2019-06-14
 */
class ApiResponseException(val code: String, val msg: ApiResponse.Msg) : RuntimeException(createErrorMessage(code, msg)) {

    constructor(response: ApiResponse) : this(response.code, response.msg)

    companion object {
        private fun createErrorMessage(code: String, msg: ApiResponse.Msg): String = msg.ko
    }
}