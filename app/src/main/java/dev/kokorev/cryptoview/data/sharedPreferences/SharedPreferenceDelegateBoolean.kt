package dev.kokorev.cryptoview.data.sharedPreferences

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.work.rxjava3.RxWorker
import dev.kokorev.cryptoview.App
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateBoolean(
    private val name: String,
    private val defaultValue: Boolean = true
): ReadWriteProperty<Any?, Boolean> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return preferences.getBoolean(name, defaultValue)
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        preferences.edit().putBoolean(name, value).apply()
    }
}

fun Fragment.preferencesBoolean(name: String) = SharedPreferenceDelegateBoolean(name)
fun Context.preferencesBoolean(name: String) = SharedPreferenceDelegateBoolean(name)
fun RxWorker.preferencesBoolean(name: String) = SharedPreferenceDelegateBoolean(name)