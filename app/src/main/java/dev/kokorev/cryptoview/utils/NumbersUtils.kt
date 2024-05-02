package dev.kokorev.cryptoview.utils

import java.text.DecimalFormat

object NumbersUtils {
    fun roundNumber(number: Double, precision: Int): Double {
        if (number <= 0) return number
        if (number >= 1) {
            val pow10 = Math.pow(10.0, precision.toDouble())
            return Math.round(number * pow10) / pow10
        } else return roundNumber(number * 10, precision) / 10
    }

    fun formatBigNumber(number: Double) : String {
        if (number > 10_000_000_000) return DecimalFormat("#,###B").format(number / 1_000_000_000)
        if (number > 10_000_000) return DecimalFormat("#,###M").format(number / 1_000_000)
        if (number > 10_000) return DecimalFormat("#,###T").format(number / 1_000)
        return DecimalFormat("#,###").format(number)
    }
}