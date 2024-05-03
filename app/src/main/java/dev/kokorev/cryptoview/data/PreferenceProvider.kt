package dev.kokorev.cryptoview.data

import android.content.Context
import android.content.SharedPreferences

// Working with SharedPreferences of the app
class PreferenceProvider(context: Context) {
    // Application context
    private val appContext = context.applicationContext
    // SharedPreferences
    private val preference: SharedPreferences = appContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)

    // Initializing SharedPreferences for the app if launched for the first time
    init {
        if(preference.getBoolean(KEY_FIRST_LAUNCH, true)) {
            preference.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        }
    }

    // Last time in millis when remote API was accessed
    fun saveLastTopMoversCallTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_LAST_TOP_MOVERS_CALL_TIME, time).apply()
    }
    fun getLastTopMoversCallTime(): Long {
        return preference.getLong(KEY_LAST_TOP_MOVERS_CALL_TIME, 0L)
    }

    // Constants
    companion object {
        private const val SETTINGS = "settings"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LAST_TOP_MOVERS_CALL_TIME = "last_top_movers_time"
    }
}