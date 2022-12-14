package com.appknot.sample

import com.appknot.core_rx.base.RxBaseActivity
import com.appknot.sample.adapter.PagingAdapter
import com.appknot.sample.databinding.ActivityPagingBinding
import com.appknot.sample.viewmodel.PagingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PagingActivity : RxBaseActivity<ActivityPagingBinding, PagingViewModel>() {

    override val layoutResourceId: Int
        get() = R.layout.activity_paging
    override val viewModel: PagingViewModel by viewModel()

    override fun initStartView() {

    }

    override fun initDataBinding() {
        with(viewDataBinding) {
            lifecycleOwner = this@PagingActivity
            adapter = PagingAdapter()
            vm = viewModel
        }
    }

    override fun initAfterBinding() {
        with(viewModel) {
        }
    }

}