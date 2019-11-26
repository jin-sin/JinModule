package com.appknot.seotda.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 *
 * @author Jin on 2019-06-25
 */


fun Context.showToast(msg: String, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, msg, duration).show()

fun Context.showToast(@StringRes StringResId: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, getString(StringResId), duration).show()
