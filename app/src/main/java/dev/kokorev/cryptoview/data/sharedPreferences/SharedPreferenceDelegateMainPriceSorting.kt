package dev.kokorev.cryptoview.data.sharedPreferences

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.views.fragments.MainPriceSorting
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateMainPriceSorting(
    private val name: String,
    private val defaultValue: MainPriceSorting = MainPriceSorting.H24
): ReadWriteProperty<Any?, MainPriceSorting> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): MainPriceSorting {
        return when(name) {
            KEY_MAIN_PRICE_SORTING -> {
                val str = preferences.getString(KEY_MAIN_PRICE_SORTING, DEFAULT_MAIN_PRICE_SORTING.str)
                (MainPriceSorting from str) ?: defaultValue
            }
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: MainPriceSorting) {
        when (name) {
            KEY_MAIN_PRICE_SORTING -> {
                preferences.edit().putString(KEY_MAIN_PRICE_SORTING, value.str).apply()
            }
        }
    }
}

fun Fragment.preferencesMainPriceSorting(name: String) = SharedPreferenceDelegateMainPriceSorting(name)
fun ViewModel.preferencesMainPriceSorting(name: String) = SharedPreferenceDelegateMainPriceSorting(name)