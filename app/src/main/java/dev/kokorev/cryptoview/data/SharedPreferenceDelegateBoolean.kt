package dev.kokorev.cryptoview.data

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.rxjava3.RxWorker
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateBoolean(
    private val name: String,
    private val defaultValue: Boolean = false
): ReadWriteProperty<Any?, Boolean> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return when(name) {
            "toCheckFavorites" -> preferences.getBoolean(PreferenceProvider.KEY_TO_CHECK_FAVORITES, true)
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        when (name) {
            "toCheckFavorites" -> {
                preferences.edit().putBoolean(PreferenceProvider.KEY_TO_CHECK_FAVORITES, value).apply()
            }
        }
    }
}

fun Fragment.preferencesBoolean(name: String) = SharedPreferenceDelegateBoolean(name)
fun Context.preferencesBoolean(name: String) = SharedPreferenceDelegateBoolean(name)
fun RxWorker.preferencesBoolean(name: String) = SharedPreferenceDelegateBoolean(name)