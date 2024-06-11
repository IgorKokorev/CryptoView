package dev.kokorev.cryptoview.data.sharedPreferences

import android.content.Context
import androidx.fragment.app.Fragment
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.views.fragments.SearchSorting
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferenceDelegateSearchSorting(
    private val name: String,
    private val defaultValue: SearchSorting = SearchSorting.RANK
): ReadWriteProperty<Any?, SearchSorting> {
    
    private val preferences by lazy {
        App.instance.applicationContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): SearchSorting {
        return when(name) {
            KEY_SEARCH_SORTING -> {
                val str = preferences.getString(KEY_SEARCH_SORTING, DEFAULT_SEARCH_SORTING.str)
                (SearchSorting from str) ?: defaultValue
            }
            else -> defaultValue
        }
    }
    
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: SearchSorting) {
        when (name) {
            KEY_SEARCH_SORTING -> {
                preferences.edit().putString(KEY_SEARCH_SORTING, value.str).apply()
            }
        }
    }
}

fun Fragment.preferencesSearchSorting(name: String) = SharedPreferenceDelegateSearchSorting(name)