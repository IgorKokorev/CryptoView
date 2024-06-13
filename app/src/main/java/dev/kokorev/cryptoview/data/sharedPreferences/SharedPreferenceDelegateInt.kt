package dev.kokorev.cryptoview.data.sharedPreferences

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import kotlin.math.sign
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateInt(
    private val name: String,
    private val defaultValue: Int = 0
): ReadWriteProperty<Any?, Int> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return when(name) {
            KEY_NUM_TOP_COINS -> preferences.getInt(KEY_NUM_TOP_COINS, DEFAULT_NUM_TOP_COINS)
            KEY_SEARCH_SORTING_DIRECTION -> preferences.getInt(
                KEY_SEARCH_SORTING_DIRECTION,
                DEFAULT_SEARCH_SORTING_DIRECTION
            )
            KEY_PORTFOLIO_NOTIFICATION_TIME -> preferences.getInt(
                KEY_PORTFOLIO_NOTIFICATION_TIME,
                DEFAULT_PORTFOLIO_NOTIFICATION_TIME
            )
            else -> preferences.getInt(name, defaultValue)
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        when (name) {
            KEY_NUM_TOP_COINS -> {
                if (value in TOP_COINS_FROM .. TOP_COINS_TO)
                    preferences.edit().putInt(KEY_NUM_TOP_COINS, value).apply()
            }
            KEY_SEARCH_SORTING_DIRECTION -> {
                if (value != 0) preferences.edit().putInt(KEY_SEARCH_SORTING_DIRECTION, value.sign).apply()
            }
            KEY_PORTFOLIO_NOTIFICATION_TIME -> {
                if (value in PORTFOLIO_NOTIFICATION_TIME_MIN .. PORTFOLIO_NOTIFICATION_TIME_MAX)
                    preferences.edit().putInt(KEY_PORTFOLIO_NOTIFICATION_TIME, value).apply()
            }
            else -> preferences.edit().putInt(name, value).apply()
        }
    }
}

fun Context.preferencesInt(name: String) = SharedPreferenceDelegateInt(name)
fun Fragment.preferencesInt(name: String) = SharedPreferenceDelegateInt(name)
fun ViewModel.preferencesInt(name: String) = SharedPreferenceDelegateInt(name)