package com.appknot.core_rx.base

import androidx.annotation.MainThread
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.appknot.core_rx.binding.BindingObservable
import com.appknot.core_rx.util.SnackbarMessage
import com.appknot.core_rx.util.SnackbarMessageString
import com.appknot.core_rx.util.ToastMessage
import com.appknot.core_rx.util.ToastMessageString
import com.appknot.core_rx.widget.timer.TimerLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.viewModelScope
import com.appknot.core_rx.binding.bindingProperty
import com.appknot.core_rx.extensions.bindingId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty

/**
 *
 * @author Jin on 2020-02-21
 */

open class RxBaseViewModel : ViewModel(), BindingObservable {

    // 일회성 이벤트를 만들어 내는 라이브 이벤트
    // 뷰는 이러한 이벤트를 바인딩하고 있다가, 적절한 상황이 되면 액티비티를 호출하거나 스낵바를 만듬
    private val snackbarMessage = SnackbarMessage()
    private val snackbarMessageString = SnackbarMessageString()

    private val toastMessage = ToastMessage()
    private val toastMessageString = ToastMessageString()

    private val timer = TimerLiveData()

    @get: Bindable
    var isLoading: Boolean by bindingProperty(false)

    private val fetchingIndex: MutableStateFlow<Int> = MutableStateFlow(0)
//    private val listFlow = fetchingIndex.flatMapLatest { page ->
//        mainRepository.fetchPokemonList(
//            page = page,
//            onStart = { isLoading = true },
//            onComplete = { isLoading = false },
//            onError = { toastMessage = it }
//        )
//    }
//
//    @get:Bindable
//    val list: List<Any> by bindingProperty(emptyList())

    /**
     * RxJava 의 observing을 위한 부분.
     * addDisposable을 이용하여 추가하기만 하면 된다
     */
    private val compositeDisposable = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
        clearAllProperties()

        compositeDisposable.clear()

        super.onCleared()
    }

    /**
     * 스낵바를 보여주고 싶으면 viewModel 에서 이 함수를 호출
     */
    fun showSnackbar(stringResourceId: Int) {
        snackbarMessage.value = stringResourceId
    }
    fun showSnackbar(str: String){
        snackbarMessageString.value = str
    }

    fun showToast(stringResourceId: Int)    {
        toastMessage.value = stringResourceId
    }

    fun showToast(str: String)  {
        toastMessageString.value = str
    }

    fun startTimer(millisInFuture: Long, countDownInterval: Long) {
        timer.millisInFuture = millisInFuture
        timer.countDownInterval = countDownInterval
        timer.value = millisInFuture
    }

    fun stopTimer() {
        timer.stopTimer()
    }

    fun removeTimerObserver() {
        timer.removeTimerObserver()
    }

    /**
     * RxBaseActivity 에서 쓰는 함수
     */
    fun observeSnackbarMessage(lifeCycleOwner: LifecycleOwner, ob:(Int) -> Unit){
        snackbarMessage.observe(lifeCycleOwner, ob)
    }
    fun observeSnackbarMessageStr(lifeCycleOwner: LifecycleOwner, ob:(String) -> Unit) {
        snackbarMessageString.observe(lifeCycleOwner, ob)
    }
    fun observeToastMessage(lifeCycleOwner: LifecycleOwner, ob:(Int) -> Unit)   {
        toastMessage.observe(lifeCycleOwner, ob)
    }
    fun observeToastMessageStr(lifeCycleOwner: LifecycleOwner, ob: (String) -> Unit) {
        toastMessageString.observe(lifeCycleOwner, ob)
    }
    fun observeTimer(lifeCycleOwner: LifecycleOwner, finishOb: (Long) -> Unit, tickOb: (Long) -> Unit)    {
        timer.observe(lifeCycleOwner, finishOb, tickOb)
    }
    fun observeTimerForever(finishOb: (Long) -> Unit, tickOb: (Long) -> Unit) {
        timer.observeForever(finishOb, tickOb)
    }

    private val lock: Any = Any()

    private var propertyCallbacks: PropertyChangeRegistry? = null

    override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks
                ?: PropertyChangeRegistry().also { propertyCallbacks = it }
            propertyCallbacks.add(callback)
        }
    }


    override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback?) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.remove(callback)
        }
    }

    override fun notifyPropertyChanged(property: KProperty<*>) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.notifyCallbacks(this, property.bindingId(), null)
        }
    }

    override fun notifyPropertyChanged(function: KFunction<*>) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.notifyCallbacks(this, function.bindingId(), null)
        }
    }

    override fun notifyPropertyChanged(bindingId: Int) {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.notifyCallbacks(this, bindingId, null)
        }
    }

    override fun notifyAllPropertiesChanged() {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.notifyCallbacks(this, BR._all, null)
        }
    }

    override fun clearAllProperties() {
        synchronized(lock) lock@{
            val propertyCallbacks = propertyCallbacks ?: return@lock
            propertyCallbacks.clear()
        }
    }


    @MainThread
    fun fetchNextList() {
        if (!isLoading) {
            fetchingIndex.value++
        }
    }

}