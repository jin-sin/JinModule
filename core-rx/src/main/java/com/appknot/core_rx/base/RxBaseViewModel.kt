package com.appknot.core_rx.base

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.appknot.core_rx.util.*
import com.appknot.core_rx.widget.timer.TimerLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 *
 * @author Jin on 2020-02-21
 */

open class RxBaseViewModel : ViewModel() {

    // 일회성 이벤트를 만들어 내는 라이브 이벤트
    // 뷰는 이러한 이벤트를 바인딩하고 있다가, 적절한 상황이 되면 액티비티를 호출하거나 스낵바를 만듬
    private val snackbarMessage = SnackbarMessage()
    private val snackbarMessageString = SnackbarMessageString()

    private val toastMessage = ToastMessage()
    private val toastMessageString = ToastMessageString()

    private val timer = TimerLiveData()

    private val intentLiveEvent = IntentLiveEvent()

    /**
     * RxJava 의 observing을 위한 부분.
     * addDisposable을 이용하여 추가하기만 하면 된다
     */
    private val compositeDisposable = CompositeDisposable()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    override fun onCleared() {
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

    fun startTimer(delay: Long, period: Long) {
        timer.period = period
        timer.postValue(delay)
    }

    fun launchActivity(intent: Intent)    {
        intentLiveEvent.value = intent
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
    fun observeTimer(lifeCycleOwner: LifecycleOwner, ob: (Long) -> Unit)    {
        timer.observe(lifeCycleOwner, ob)
    }
    fun observeIntent(lifeCycleOwner: LifecycleOwner, ob: (Intent) -> Unit) {
        intentLiveEvent.observe(lifeCycleOwner, ob)
    }
}