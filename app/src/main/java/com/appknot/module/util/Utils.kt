package com.appknot.module.util

import java.text.DecimalFormat
import java.util.*

/**
 *
 * @author Jin on 2019-07-04
 */

/**
 * Int 형 숫자를 받아 1000 단위로 , 를 표시한다
 * */
fun Int.convertCurrency(): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(this)
}


/**
 * Int 형 milliseconds 를 받아 00:00 형식으로 표시한다
 * */
fun Int.stringForTime(): String {
    val totalSeconds = this / 1000

    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600

    val formatBuilder = StringBuilder()
    val formatter = Formatter(formatBuilder, Locale.getDefault())
    return if (hours > 0) {
        formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
    } else {
        formatter.format("%02d:%02d", minutes, seconds).toString()
    }
}