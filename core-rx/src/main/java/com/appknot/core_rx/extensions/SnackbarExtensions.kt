package com.appknot.core_rx.extensions

import android.app.Activity
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.appknot.core_rx.R
import com.google.android.material.snackbar.Snackbar

/**
 *
 * @author Jin on 2019-06-25
 */



fun Activity.showSnackbar(msg: String) {
    Snackbar.make(this.window.decorView.findViewById(android.R.id.content),
        msg,
        Snackbar.LENGTH_LONG)
        .setActionTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        .show()
}


fun Activity.showSnackbar(@StringRes StringResId: Int) {
    showSnackbar(getString(StringResId))
}
