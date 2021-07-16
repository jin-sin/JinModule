package com.appknot.core_rx.binding

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.appknot.core_rx.adapter.BindingListAdapter
import com.appknot.core_rx.base.RxBaseViewModel
import com.appknot.core_rx.extensions.whatIfNotNullAs
import com.appknot.core_rx.widget.RecyclerViewPaginator

/**
 *
 * @author Jin on 2021/07/07
 */
object RecyclerViewBinding {

    @JvmStatic
    @BindingAdapter("adapter")
    fun bindAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        view.adapter = adapter.apply {
            stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    @JvmStatic
    @BindingAdapter("submitList")
    fun bindSubmitList(view: RecyclerView, itemList: List<Any>?) {
        view.adapter.whatIfNotNullAs<BindingListAdapter<Any, *>> { adapter ->
            adapter.submitList(itemList)
        }
    }

    @JvmStatic
    @BindingAdapter("paginationList")
    fun paginationList(view: RecyclerView, viewModel: RxBaseViewModel) {
        RecyclerViewPaginator(
            recyclerView = view,
            isLoading = { viewModel.isLoading },
            loadMore = { viewModel.fetchNextList() },
            onLast = { false }
        ).run {
            threshold = 8
        }
    }
}