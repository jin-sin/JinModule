package com.appknot.module.network

/**
 * appknot 컨벤션 기반 API 응답 구조
 * @author Xellsky(Alex Ji)
 */
class ApiResponse {
    lateinit var code: String
    lateinit var msg: Msg
    lateinit var data: Any

    inner class Msg {
        lateinit var ko: String
        lateinit var en: String
    }
}