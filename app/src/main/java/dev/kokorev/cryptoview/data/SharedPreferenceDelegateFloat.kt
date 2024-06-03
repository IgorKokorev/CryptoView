package dev.kokorev.cryptoview.data

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.rxjava3.RxWorker
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateFloat(
    private val name: String,
    private val defaultValue: Float = 0.0f
): ReadWriteProperty<Any?, Float> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return when(name) {
            "favoriteChange" -> preferences.getFloat(
                PreferenceProvider.KEY_FAVORITE_MIN_CHANGE,
                PreferenceProvider.DEFAULT_FAVORITE_MIN_CHANGE
            )
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        when (name) {
            "favoriteChange" -> {
                if (value in Constants.FAVORITE_CHECK_MIN_CHANGE.. Constants.FAVORITE_CHECK_MAX_CHANGE)
                    preferences.edit().putFloat(PreferenceProvider.KEY_FAVORITE_MIN_CHANGE, value).apply()
            }
        }
    }
}

fun Fragment.preferencesFloat(name: String) = SharedPreferenceDelegateFloat(name)
fun RxWorker.preferencesFloat(name: String) = SharedPreferenceDelegateFloat(name)