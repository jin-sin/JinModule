package com.appknot.seotda.extensions

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.appknot.module.R
import java.util.ArrayList

/**
 *
 * @author Jin on 2019-06-25
 */


private var loadingDialogList: ArrayList<AlertDialog?> = ArrayList()

fun Context.showLoadingDialog() {
    loadingDialogList.add(
        AlertDialog.Builder(this)
            .setView(View.inflate(this, R.layout.dialog_progress, null))
            .setCancelable(false)
            .create().apply {
                window?.setBackgroundDrawableResource(android.R.color.transparent)
                window?.setDimAmount(0F)
                show()
            }
    )
}

fun Context.hideLoadingDialog() {
    loadingDialogList.forEach { it?.dismiss() }
    loadingDialogList.clear()
}