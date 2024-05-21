package dev.kokorev.cryptoview.utils

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.kokorev.cryptoview.R
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

    fun setPrice(price: Double?): String {
        return if (price == null) "-"
        else DecimalFormat("#,###.########").format(
            NumbersUtils.roundNumber(
                price,
                3
            )
        )
    }

    fun setPriceChange(percentChange: Double?, context: Context, view: TextView) {
        val changeNumber = percentChange ?: 0.0
        var changeString = DecimalFormat("#,##0.00").format(
            NumbersUtils.roundNumber(
                changeNumber,
                2
            )
        ) + "%"
        if (changeNumber < 0) {
            view.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.red
                )
            )
        } else if (changeNumber > 0) {
            changeString = '+' + changeString
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