package dev.kokorev.cryptoview.utils

import android.content.Context
import android.icu.text.NumberFormat
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.kokorev.cryptoview.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

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
    
    
    fun getPortfolioNotificationMillis(portfolioNotificationTime: Int): Long {
        
        val hour = portfolioNotificationTime / 60
        val minute = portfolioNotificationTime - hour * 60
        
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val now = Calendar.getInstance()
        if (now.after(calendar)) calendar.add(Calendar.HOUR, 24)
        return calendar.timeInMillis
    }
    
}


fun Instant.toLocalDate(): LocalDate {
    val localDateTime = LocalDateTime.ofInstant(this, ZoneId.of("UTC"))
    return localDateTime.toLocalDate()
}

fun LocalDate.toInstant(): Instant {
    val localDateTime = this.atStartOfDay(ZoneId.of("UTC"))
    return localDateTime.toInstant()
}