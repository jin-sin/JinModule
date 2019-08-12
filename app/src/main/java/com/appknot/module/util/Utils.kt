package com.appknot.module.util

import java.text.DecimalFormat

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