package com.appknot.sample.viewmodel

import androidx.databinding.Bindable
import androidx.lifecycle.viewModelScope
import com.appknot.core_rx.base.RxBaseViewModel
import com.appknot.core_rx.binding.asBindingProperty
import com.appknot.core_rx.binding.bindingProperty
import com.appknot.core_rx.extensions.onError
import com.appknot.core_rx.extensions.onException
import com.appknot.core_rx.extensions.suspendOnSuccess
import com.appknot.sample.api.SampleApi
import com.appknot.sample.model.PassengerInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PagingViewModel(val api: SampleApi) : RxBaseViewModel() {

    private val listFlow = fetchingIndex.flatMapLatest { page ->
        getPassengers(page)
    }

    @get:Bindable
    val passengerList: ArrayList<Pokemon> by listFlow.asBindingProperty(viewModelScope, ArrayList())

    val tempList = arrayListOf<PassengerInfo.Passenger>()

    suspend fun getPassengers(page: Int = 0) =
        flow {
            api.fetchPokemonList(10, page * 10).suspendOnSuccess {
                emit(data.results)
            }.onError {
                showToast("onError")
            }.onException {
                viewModelScope.launch(Dispatchers.Main) {
                    showToast("통신에 실패 했습니다")
                }
            }
        }.onStart { isLoading = true }.onCompletion { isLoading = false }.flowOn(Dispatchers.IO)
}