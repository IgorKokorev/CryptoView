package dev.kokorev.cryptoview.utils

import android.content.Context
import android.icu.text.NumberFormat
import android.icu.util.ULocale
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.kokorev.cryptoview.R

object NumbersUtils {
    // get user locale
    private val uloc: ULocale = try {
        ULocale.getAvailableLocales().get(0)
    } catch (e: Exception) {
        ULocale("en")
    }

    const val MIN_PRECISION = 2
    const val MAX_PRECISION = 7

    val numberFormat = NumberFormat.getInstance(uloc).apply {
        minimumFractionDigits = MIN_PRECISION
        maximumFractionDigits = MIN_PRECISION
        minimumIntegerDigits = 1
        isGroupingUsed = true
    }

    fun formatPriceUSD(price: Double?): String =
        if (price == null) "-"
        else formatPrice(price) + "$"

    fun roundNumber(number: Double, precision: Int): Double {
        if (number == 0.0) return number
        if (number < 0) return -roundNumber(-number, precision)
        if (number > 10 && precision > 2) return Math.round(number * 100.0) / 100.0
        if (number >= 1) {
            val pow10 = Math.pow(10.0, precision.toDouble())
            return Math.round(number * pow10) / pow10
        } else return roundNumber(number * 10, precision) / 10
    }

    fun getPrecision(number: Double): Int {
        if (number == 0.0) return MIN_PRECISION

        for (i in 0 until (MAX_PRECISION - MIN_PRECISION)) {
            if (Math.pow(10.0, i.toDouble()) * number > 10) return (MIN_PRECISION + i)
        }
        return MAX_PRECISION
    }

    fun formatBigNumber(number: Double): String {
        return when {
            (number > 1_000_000_000_000) -> formatWithPrecision(number / 1_000_000_000.0, 0) + "B"
            (number > 100_000_000_000) -> formatWithPrecision(number / 1_000_000_000.0, 1) + "B"
            (number > 10_000_000_000) -> formatWithPrecision(number / 1_000_000_000.0, 2) + "B"
            (number > 1_000_000_000) -> formatWithPrecision(number / 1_000_000.0, 0) + "M"
            (number > 100_000_000) -> formatWithPrecision(number / 1_000_000.0, 1) + "M"
            (number > 10_000_000) -> formatWithPrecision(number / 1_000_000.0, 2) + "M"
            (number > 1_000_000) -> formatWithPrecision(number / 1_000.0, 0) + "T"
            (number > 100_000) -> formatWithPrecision(number / 1_000.0, 1) + "T"
            (number > 10_000) -> formatWithPrecision(number / 1_000.0, 2) + "T"
            (number > 1_000) -> formatWithPrecision(number, 0)
            
            
            else -> formatPrice(number)
        }
    }

    fun formatPrice(price: Double?): String {
        return if (price == null) "0"
        else formatWithPrecision(price, getPrecision(price))
    }
    
    fun formatWithPrecision(num: Double, precision: Int): String {
        numberFormat.maximumFractionDigits = precision
        return numberFormat.format(num)
    }

    fun parseDouble(str: String): Double {
        return numberFormat.parse(str).toDouble()
    }

    fun setChangeView(change: Double?, context: Context, view: TextView, suffix: String = "") {
        val changeNumber = change ?: 0.0
        numberFormat.maximumFractionDigits = MIN_PRECISION
        var changeString = numberFormat.format(changeNumber) + suffix
        if (changeNumber < 0) {
            view.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.red
                )
            )
        } else if (changeNumber > 0) {
            changeString = "+$changeString"
            view.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.green
                )
            )
        }
        view.text = changeString
    }

    fun formatBigNumberShort(number: Long): String {
        return if (number >= 1_000_000_000) formatWithPrecision(number / 1_000_000_000.0, 0) + "B"
        else if (number >= 1_000_000)  formatWithPrecision(number / 1_000_000.0, 0) + "M"
        else if (number >= 1_000) formatWithPrecision(number / 1_000.0, 0) + "T"
        else formatWithPrecision(number.toDouble(), 0)
    }
}