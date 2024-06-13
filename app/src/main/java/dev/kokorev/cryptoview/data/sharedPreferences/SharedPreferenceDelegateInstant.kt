package dev.kokorev.cryptoview.data.sharedPreferences

import android.app.Service
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import java.time.Instant
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateInstant(
    private val name: String,
    private val defaultValue: Instant = Instant.ofEpochMilli(0L)
) : ReadWriteProperty<Any?, Instant> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    
    override fun getValue(thisRef: Any?, property: KProperty<*>): Instant {
        return Instant.ofEpochMilli(preferences.getLong(name, defaultValue.toEpochMilli()))
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Instant) {
        preferences.edit().putLong(name, value.toEpochMilli()).apply()
    }
}

fun Context.preferencesInstant(name: String) = SharedPreferenceDelegateInstant(name)
fun Fragment.preferencesInstant(name: String) = SharedPreferenceDelegateInstant(name)
fun ViewModel.preferencesInstant(name: String) = SharedPreferenceDelegateInstant(name)
fun Service.preferencesInstant(name: String) = SharedPreferenceDelegateInstant(name)