package dev.kokorev.cryptoview.data

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateLong(
    private val name: String,
    private val defaultValue: Long = 0L
): ReadWriteProperty<Any?, Long> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): Long {
        return when(name) {
            "minMcap" -> preferences.getLong(PreferenceProvider.KEY_MIN_MCAP, PreferenceProvider.DEFAULT_MIN_MCAP)
            "minVol" -> preferences.getLong(PreferenceProvider.KEY_MIN_VOL, PreferenceProvider.DEFAULT_MIN_VOL)
            "cpTickersTime" -> preferences.getLong(PreferenceProvider.KEY_CP_TICKERS_LAST_CALL_TIME, 0L)
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) {
        when (name) {
            "minMcap" -> {
                if (Constants.minMCaps.contains(value))
                    preferences.edit().putLong(PreferenceProvider.KEY_MIN_MCAP, value).apply()
            }
            "minVol" -> {
                if (Constants.minVols.contains(value))
                    preferences.edit().putLong(PreferenceProvider.KEY_MIN_VOL, value).apply()
            }
            "cpTickersTime" -> {
                val time = if (value == 0L) System.currentTimeMillis() else value
                preferences.edit().putLong(PreferenceProvider.KEY_CP_TICKERS_LAST_CALL_TIME, time).apply()
            }
        }
    }
}

fun Fragment.preferencesLong(name: String) = SharedPreferenceDelegateLong(name)
fun ViewModel.preferencesLong(name: String) = SharedPreferenceDelegateLong(name)