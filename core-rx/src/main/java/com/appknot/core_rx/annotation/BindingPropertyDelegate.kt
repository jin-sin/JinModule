package com.appknot.core_rx.annotation

/**
 *
 * @author Jin on 2021/07/07
 */


/**
 * 바인딩 속성 및 함수를 표시하는 데 이 주석을 사용해야 합니다.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@DslMarker
@Retention(AnnotationRetention.BINARY)
internal annotation class BindingPropertyDelegate