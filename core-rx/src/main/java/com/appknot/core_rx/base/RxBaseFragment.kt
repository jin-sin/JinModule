package com.appknot.core_rx.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

/**
 *
 * @author Jin on 2020-02-24
 */

abstract class RxBaseFragment<T: ViewDataBinding, R: BaseViewModel> : Fragment()    {
    lateinit var viewDataBinding: T

    abstract val layoutResourceId: Int

    abstract val viewModel: R

    abstract fun initStartView()

    abstract fun initDataBinding()

    abstract fun initAfterBinding()

    lateinit var currentView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewDataBinding = DataBindingUtil.inflate(
            inflater,
            layoutResourceId,
            container,
            false
        )
        currentView = viewDataBinding.root

        return currentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        snackbarObserving()
        toastObserving()
        initStartView()
        initDataBinding()
        initAfterBinding()
    }

    private fun snackbarObserving() {
        viewModel.observeSnackbarMessage(this) {
            activity?.window?.decorView?.let { view ->
                Snackbar.make(
                    view.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        viewModel.observeSnackbarMessageStr(this){
            activity?.window?.decorView?.let { view ->
                Snackbar.make(
                    view.findViewById(android.R.id.content),
                    it,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun toastObserving()    {
        viewModel.observeToastMessage(this) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.observeSnackbarMessageStr(this)   {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }
}