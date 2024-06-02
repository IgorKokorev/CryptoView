package dev.kokorev.cryptoview.utils

import android.content.Context
import android.icu.text.NumberFormat
import android.icu.util.ULocale
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.kokorev.cryptoview.R
import java.text.DecimalFormat

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
        else DecimalFormat("#,##0.00######$").format(
            roundNumber(
                price,
                3
            )
        )

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
        if (number > 1_000_000_000_000) return DecimalFormat("#,###B").format(number / 1_000_000_000.0)
        if (number > 100_000_000_000) return DecimalFormat("#,###.0B").format(number / 1_000_000_000.0)
        if (number > 10_000_000_000) return DecimalFormat("#,###.00B").format(number / 1_000_000_000.0)
        if (number > 1_000_000_000) return DecimalFormat("#,###M").format(number / 1_000_000.0)
        if (number > 100_000_000) return DecimalFormat("#,###.0M").format(number / 1_000_000.0)
        if (number > 10_000_000) return DecimalFormat("#,###.00M").format(number / 1_000_000.0)
        if (number > 1_000_000) return DecimalFormat("#,###T").format(number / 1_000.0)
        if (number > 100_000) return DecimalFormat("#,###.0T").format(number / 1_000.0)
        if (number > 10_000) return DecimalFormat("#,###.00T").format(number / 1_000.0)
        return DecimalFormat("#,###").format(number)
    }

    fun formatPrice(price: Double?): String {
        return if (price == null) "-"
        else {
            numberFormat.maximumFractionDigits = getPrecision(price)
            numberFormat.format(price)
        }
    }

    fun parseDouble(str: String): Double {
        return numberFormat.parse(str).toDouble()
    }

    fun setChangeView(change: Double?, context: Context, view: TextView, suffix: String) {
        val changeNumber = change ?: 0.0
        var changeString = DecimalFormat("#,##0.00").format(
            NumbersUtils.roundNumber(
                changeNumber,
                2
            )
        ) + suffix
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
        if (number >= 1_000_000_000) return DecimalFormat("#,###B").format(number / 1_000_000_000)
        if (number >= 1_000_000) return DecimalFormat("#,###M").format(number / 1_000_000)
        if (number >= 1_000) return DecimalFormat("#,###T").format(number / 1_000)
        return DecimalFormat("#,###").format(number)
    }
}