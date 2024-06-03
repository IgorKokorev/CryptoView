package dev.kokorev.cryptoview.data

import android.content.Context
import androidx.fragment.app.Fragment
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
private const val defaultTime = "2000-01-01 00:00"

class SharedPreferenceDelegateDateTime(
    private val name: String,
    private val defaultValue: LocalDateTime = LocalDateTime.parse(defaultTime, dateTimeFormatter)
): ReadWriteProperty<Any?, LocalDateTime> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): LocalDateTime {
        return when(name) {
            "tmSentimentTime" -> {
                val timeStr = preferences.getString(PreferenceProvider.KEY_TM_SENTIMENT_LAST_CALL_TIME, defaultTime) ?: defaultTime
                return LocalDateTime.parse(timeStr, dateTimeFormatter)
            }
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: LocalDateTime) {
        when (name) {
            "tmSentimentTime" -> {
                val timeStr = value.withMinute(0).withSecond(0).withNano(0).format(dateTimeFormatter)
                preferences.edit().putString(PreferenceProvider.KEY_TM_SENTIMENT_LAST_CALL_TIME, timeStr).apply()
            }
        }
    }
}

fun Fragment.preferencesDateTime(name: String) = SharedPreferenceDelegateDateTime(name)