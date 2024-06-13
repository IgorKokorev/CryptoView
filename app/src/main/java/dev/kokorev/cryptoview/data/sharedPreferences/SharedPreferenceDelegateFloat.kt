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
) : ReadWriteProperty<Any?, Float> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return preferences.getFloat(
            name,
            when (name) {
                KEY_FAVORITE_CHANGE -> DEFAULT_FAVORITE_CHANGE
                else -> defaultValue
            }
        )
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        when (name) {
            KEY_FAVORITE_CHANGE -> {
                if (value in FAVORITE_MIN_CHANGE .. FAVORITE_MAX_CHANGE)
                    preferences.edit().putFloat(KEY_FAVORITE_CHANGE, value).apply()
            }
            else -> preferences.edit().putFloat(name, value).apply()
        }
    }
}

fun Context.preferencesFloat(name: String) = SharedPreferenceDelegateFloat(name)
fun Fragment.preferencesFloat(name: String) = SharedPreferenceDelegateFloat(name)
fun RxWorker.preferencesFloat(name: String) = SharedPreferenceDelegateFloat(name)