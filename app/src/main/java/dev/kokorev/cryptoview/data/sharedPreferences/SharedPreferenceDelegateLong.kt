package dev.kokorev.cryptoview.data.sharedPreferences

import android.app.Service
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateLong(
    private val name: String,
    private val defaultValue: Long = 0L
): ReadWriteProperty<Any?, Long> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return when(name) {
            "minMcap" -> preferences.getLong(KEY_MIN_MCAP, DEFAULT_MIN_MCAP)
            "minVol" -> preferences.getLong(KEY_MIN_VOL, DEFAULT_MIN_VOL)
            "cpTickersTime" -> preferences.getLong(KEY_CP_TICKERS_LAST_CALL_TIME, 0L)
            "portfolioEvaluationTime" -> preferences.getLong(KEY_LAST_PORTFOLIO_EVALUATION_TIME, 0L)
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        when (name) {
            "minMcap" -> {
                if (MIN_MCAPS.contains(value))
                    preferences.edit().putLong(KEY_MIN_MCAP, value).apply()
            }
            "minVol" -> {
                if (MIN_VOLS.contains(value))
                    preferences.edit().putLong(KEY_MIN_VOL, value).apply()
            }
            "cpTickersTime" -> {
                val time = if (value == 0L) System.currentTimeMillis() else value
                preferences.edit().putLong(KEY_CP_TICKERS_LAST_CALL_TIME, time).apply()
            }
            "portfolioEvaluationTime" -> {
                val time = if (value == 0L) System.currentTimeMillis() else value
                preferences.edit().putLong(KEY_LAST_PORTFOLIO_EVALUATION_TIME, time).apply()
            }
        }
    }
}

fun Fragment.preferencesLong(name: String) = SharedPreferenceDelegateLong(name)
fun ViewModel.preferencesLong(name: String) = SharedPreferenceDelegateLong(name)
fun Service.preferencesLong(name: String) = SharedPreferenceDelegateLong(name)
