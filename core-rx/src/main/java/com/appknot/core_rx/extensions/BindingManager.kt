package com.appknot.core_rx.extensions

import androidx.databinding.Bindable
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.hasAnnotation
import androidx.databinding.library.baseAdapters.BR

/**
 *
 * @author Jin on 2021/07/06
 */
object BindingManager {

    /** BR 클래스에서 생성 된 필드의 정보를 보유하기 위한 map. */
    @PublishedApi
    internal var bindingFieldsMap: Map<String, Int> = emptyMap()

    /** boolean 을 표시하기 위한 Java Bean 규칙. */
    private const val JAVA_BEANS_BOOLEAN: String = "is"

    /** getter 를 표시하기 위한 Java Bean 규칙. */
    private const val JAVA_BEANS_GETTER: String = "get"

    /** setter 를 표시하기 위한 Java Bean 규칙. */
    private const val JAVA_BEANS_SETTER: String = "set"

    /**
     * BR` class 를 [BindingManager] 에 바인드 합니다.
     * 이 메소드는 앱에서 한번만 실행돼야 합니다.
     * `BR` 클래스는 [BindingManager]에 의해 쪼개질 것이고, 바인딩 필드는 특성의 적절한 바인딩 ID를 찾는 데 사용됩니다.
     *
     */
    inline fun <reified T> bind(): Int {
        synchronized(this) {
            if (bindingFieldsMap.isNotEmpty()) return@synchronized
            bindingFieldsMap = BR::class.java.fields.asSequence()
                .map { it.name to it.getInt(null) }.toMap()
        }

        return bindingFieldsMap.size
    }

    /**
     * property 에 의한 binding ID 를 리턴한다.
     *
     * @param property A kotlin [androidx.databinding.Bindable] property for finding proper binding ID.
     */
    internal fun getBindingIdByProperty(property: KProperty<*>): Int {
        val bindingProperty = property.takeIf {
            it.getter.hasAnnotation<Bindable>()
        }
            ?: throw IllegalArgumentException("KProperty: ${property.name} must be annotated with the `@Bindable` annotation on the getter.")
        val propertyName = bindingProperty.name.decapitalize(Locale.ENGLISH)
        val bindingPropertyName = propertyName
            .takeIf { it.startsWith(JAVA_BEANS_BOOLEAN) }
            ?.replaceFirst(JAVA_BEANS_BOOLEAN, String())
            ?.decapitalize(Locale.ENGLISH) ?: propertyName
        return bindingFieldsMap[bindingPropertyName] ?: BR._all
    }

    /**
     * Returns proper binding ID by function.
     *
     * @param function A kotlin [androidx.databinding.Bindable] function for finding proper binding ID.
     */
    internal fun getBindingIdByFunction(function: KFunction<*>): Int {
        val bindingFunction = function.takeIf {
            it.hasAnnotation<Bindable>()
        }
            ?: throw IllegalArgumentException("KFunction: ${function.name} must be annotated with the `@Bindable` annotation.")
        val functionName = bindingFunction.name.decapitalize(Locale.ENGLISH)
        val bindingFunctionName = when {
            functionName.startsWith(JAVA_BEANS_GETTER) -> functionName.replaceFirst(JAVA_BEANS_GETTER, String())
            functionName.startsWith(JAVA_BEANS_SETTER) -> functionName.replaceFirst(JAVA_BEANS_SETTER, String())
            functionName.startsWith(JAVA_BEANS_BOOLEAN) -> functionName.replaceFirst(JAVA_BEANS_BOOLEAN, String())
            else -> throw IllegalArgumentException("@Bindable associated with method must follow JavaBeans convention $functionName")
        }.decapitalize(Locale.ENGLISH)
        return bindingFieldsMap[bindingFunctionName] ?: BR._all
    }
}