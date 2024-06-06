package dev.kokorev.cryptoview.data.sharedPreferences

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.rxjava3.RxWorker
import dev.kokorev.cryptoview.App
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateFloat(
    private val name: String,
    private val defaultValue: Float = 0.0f
): ReadWriteProperty<Any?, Float> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return when(name) {
            "favoriteChange" -> preferences.getFloat(
                KEY_FAVORITE_MIN_CHANGE,
                DEFAULT_FAVORITE_MIN_CHANGE
            )
            "portfolioNotificationTime" -> preferences.getFloat(
                KEY_PORTFOLIO_NOTIFICATION_TIME,
                DEFAULT_PORTFOLIO_NOTIFICATION_TIME
            )
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        when (name) {
            "favoriteChange" -> {
                if (value in FAVORITE_CHECK_MIN_CHANGE.. FAVORITE_CHECK_MAX_CHANGE)
                    preferences.edit().putFloat(KEY_FAVORITE_MIN_CHANGE, value).apply()
            }
            "portfolioNotificationTime" -> {
            if (value in PORTFOLIO_NOTIFICATION_TIME_MIN .. PORTFOLIO_NOTIFICATION_TIME_MAX)
                preferences.edit().putFloat(KEY_PORTFOLIO_NOTIFICATION_TIME, value).apply()
        }
        }
    }
}

fun Fragment.preferencesFloat(name: String) = SharedPreferenceDelegateFloat(name)
fun RxWorker.preferencesFloat(name: String) = SharedPreferenceDelegateFloat(name)