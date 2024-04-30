package dev.kokorev.cryptoview.utils

object NumbersUtils {
    fun roundNumber(number: Double, precision: Int): Double {
        if (number <= 0) return number
        if (number >= 1) {
            val pow10 = Math.pow(10.0, precision.toDouble())
            return Math.round(number * pow10) / pow10
        } else return roundNumber(number * 10, precision) / 10
    }
}