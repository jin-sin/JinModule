package com.appknot.core_rx.extensions

import com.appknot.core_rx.api.ApiResponse
import com.appknot.core_rx.api.ApiResponseException
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Response
import java.util.*

/**
 *
 * @author Jin on 2019-06-14
 */

val CODE_SUCCESS = "0"

/**
 * Rx로 구현한 ApiResponse 기반의 응답처리 익스텐션
 * 1회성의 Observable (Single) 을 리턴하여 구독할수 있도록 한
 * */
fun <T : Response<ApiResponse>> Single<T>.api(): Single<ApiResponse> =
    networkThread()
        .flatMap { response ->
            when (response.code()) {
                200 -> {
                    response.body()?.let {
                        when (it.code) {
                            CODE_SUCCESS -> Single.just(it)
                            else -> Single.error(ApiResponseException(it))
                        }
                    }
                }
                else -> Single.error(Throwable("통신에 실패했습니다."))
            }
        }

fun <T> Single<T>.networkThread(): Single<T> =
    subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())


/**
 * JSON 정보 담고 있는 맵을 자바 객체로 변환
 * @param modelType 결과를 담을 자바 객체 클래스 타입
 * @return 변환 완료된 자바 객체 클래스 타입의 인스턴스
 */
fun <T> Any.parse(modelType: Class<T>): T {
    val jsonStr = Gson().toJson(this)
    return Gson().fromJson(jsonStr, modelType)
}

fun <T> String.parse(modelType: Class<T>): T {
    return Gson().fromJson(this, modelType)
}

/**
 * JSON 정보 담고 있는 맵 형식 객체를 자바 객체 콜렉션으로 변환
 * @param modelType 결과를 담을 자바 객체 배열 클래스 타입
 * @return 변환 완료된 자바 객체 클래스 타입의 콜렉션 인스턴스
 */
fun <T> Any.parse(modelType: Class<Array<T>>): ArrayList<T> {
    val jsonStr = Gson().toJson(this)
    val resultArr = Gson().fromJson(jsonStr, modelType)
    return ArrayList(resultArr.asList())
}

fun Any.toMap(): LinkedTreeMap<Any, *> {
    return try {
        this as LinkedTreeMap<Any, *>
    } catch (e: ClassCastException) {
        LinkedTreeMap<Any, Any>()
    }
}

fun Any.toList(): ArrayList<*> {
    return try {
        this as ArrayList<*>
    } catch (e: ClassCastException) {
        ArrayList<String>()
    }
}