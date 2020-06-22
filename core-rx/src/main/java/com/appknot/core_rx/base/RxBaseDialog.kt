package com.appknot.core_rx.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 *
 * @author Jin on 2020/06/22
 */

abstract class RxBaseDialog<T : ViewDataBinding, R : RxBaseViewModel>(context: Context) : Dialog(context) {

    lateinit var viewDataBinding: T

    abstract val layoutResourceId: Int

    abstract val viewModel: R

    abstract fun initStartView()

    abstract fun initDataBinding()

    abstract fun initAfterBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewDataBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                layoutResourceId,
                null,
                false
            )
        setContentView(viewDataBinding.root)

        initStartView()
        initDataBinding()
        initAfterBinding()

    }
}