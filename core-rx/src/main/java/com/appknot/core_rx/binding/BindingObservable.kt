package com.appknot.core_rx.binding

import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 *
 * @author Jin on 2021/07/05
 */
interface BindingObservable : Observable {
    /**
     * 이 함수는 [androidx.databinding.Bindable] 속성을 수신하고 나열된 속성에 대한 변경 알림이 있으면 이 값이 새로 고쳐집니다.
     * @param A 함수 [androidx.databinding.Bindable]을 변경 해야하는 속성입니다.
     */
    fun notifyPropertyChanged(property: KProperty<*>)

    /**
     * 이 함수는 [androidx.databinding.Bindable] 함수를 수신하고 나열된 속성 중 하나의 변경 알림이있는 경우 이 값이 새로 고쳐집니다.
     *
     * @param A 함수 [androidx.databinding.Bindable]을 변경 해야하는 속성입니다.
     */
    fun notifyPropertyChanged(function: KFunction<*>)

    /**
     * 특정 속성이 [PropertyChangeRegistry]에서 일치하는 항목이 변경되었습니다.
     * 이 함수는 속성 이름에 따라 데이터 바인딩 ID를 받고 나열된 속성 중 하나의 변경 알림이있는 경우 이 값이 새로 고쳐집니다.
     *
     * @param bindId 변경 해야하는 특정 데이터 바인딩 ID (생성 된 BR ID).
     */
    fun notifyPropertyChanged(bindingId: Int)

    /**
     * 이 인스턴스의 모든 속성이 변경되었음을 알립니다.
     */
    fun notifyAllPropertiesChanged()

    /**
     * 콜백 레지스트리에서 모든 바인딩 속성을 지웁니다.
     */
    fun clearAllProperties()
}