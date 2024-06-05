package dev.kokorev.cryptoview.data.sharedPreferences

import android.content.Context
import androidx.fragment.app.Fragment
import dev.kokorev.cryptoview.App
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateString(
    private val name: String,
    private val defaultValue: String =""
): ReadWriteProperty<Any?, String> {
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return preferences.getString(name, defaultValue) ?: defaultValue
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        preferences.edit().putString(name, value).apply()
    }
}

fun Context.sharedPreferences(name: String) = SharedPreferenceDelegateString(name)
fun Fragment.preferences(name: String) = SharedPreferenceDelegateString(name)