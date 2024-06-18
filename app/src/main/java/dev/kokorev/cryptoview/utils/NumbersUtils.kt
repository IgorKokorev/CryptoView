package dev.kokorev.cryptoview.utils

import android.content.Context
import android.icu.text.NumberFormat
import android.icu.util.Calendar
import android.widget.TextView
import androidx.core.content.ContextCompat
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

object NumbersUtils {
    // get user locale
    private val locale: Locale = Locale.getAvailableLocales().firstOrNull() ?: Locale.ENGLISH

    const val MIN_PRECISION = 2
    const val MAX_PRECISION = 7

    val numberFormat = NumberFormat.getInstance(locale).apply {
        minimumFractionDigits = MIN_PRECISION
        maximumFractionDigits = MIN_PRECISION
        minimumIntegerDigits = 1
        isGroupingUsed = true
    }

    fun formatPriceWithCurrency(price: Double?, currencySymbol: String = "$"): String =
        if (price == null) "-"
        else formatPrice(price) + currencySymbol


    fun getPrecision(number: Double): Int {
        if (number == 0.0) return MIN_PRECISION
        val magnitude = Math.log10(Math.abs(number)).toInt().coerceAtMost(3)
        return (MIN_PRECISION + 1 - magnitude).coerceAtMost(MAX_PRECISION)
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
        numberFormat.minimumFractionDigits = precision
        return numberFormat.format(num)
    }

    fun parseDouble(str: String): Double {
        return try {
            numberFormat.parse(str)?.toDouble() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }
    
    fun setChangeView(context: Context, view: TextView, change: Double?, suffix: String = "") {
        val changeNumber = change ?: 0.0
        numberFormat.maximumFractionDigits = MIN_PRECISION
        numberFormat.minimumFractionDigits = MIN_PRECISION
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
        return when {
            number >= 1_000_000_000 -> formatWithPrecision(number / 1_000_000_000.0, 0) + "B"
            number >= 1_000_000 -> formatWithPrecision(number / 1_000_000.0, 0) + "M"
            number >= 1_000 -> formatWithPrecision(number / 1_000.0, 0) + "T"
            else -> formatWithPrecision(number.toDouble(), 0)
        }
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

fun min(a: Instant, b: Instant): Instant = if (a.isBefore(b)) a else b

fun getColorHex(colorResource: Int) =
    Integer.toHexString(ContextCompat.getColor(App.instance.applicationContext, colorResource))
        .substring(2)