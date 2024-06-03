package dev.kokorev.cryptoview.data

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import kotlin.math.sign
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateInt(
    private val name: String,
    private val defaultValue: Int = 0
): ReadWriteProperty<Any?, Int> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(Constants.SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return when(name) {
            "nTopCoins" -> preferences.getInt(PreferenceProvider.KEY_NUM_TOP_COINS, Constants.TOP_COINS_DEFAULT)
            "searchSortingDirection" -> preferences.getInt(
                PreferenceProvider.KEY_SEARCH_SORTING_DIRECTION,
                PreferenceProvider.DEFAULT_SEARCH_SORTING_DIRECTION
            )
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        when (name) {
            "nTopCoins" -> {
                if (value in Constants.TOP_COINS_FROM.. Constants.TOP_COINS_TO)
                    preferences.edit().putInt(PreferenceProvider.KEY_NUM_TOP_COINS, value).apply()
            }
            "searchSortingDirection" -> {
                if (value != 0) preferences.edit().putInt(PreferenceProvider.KEY_SEARCH_SORTING_DIRECTION, value.sign)
                    .apply()
            }
        }
    }
}

fun Fragment.preferencesInt(name: String) = SharedPreferenceDelegateInt(name)
fun ViewModel.preferencesInt(name: String) = SharedPreferenceDelegateInt(name)