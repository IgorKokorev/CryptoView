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

    // Last time in millis when remote API was accessed to get Top Movers
    fun saveLastTopMoversCallTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_LAST_TOP_MOVERS_CALL_TIME, time).apply()
    }
    fun getLastTopMoversCallTime(): Long = preference.getLong(KEY_LAST_TOP_MOVERS_CALL_TIME, 0L)

    // Last time in millis when remote API was accessed to get all CoinPaprika tickers
    fun getLastCpTickersCallTime(): Long = preference.getLong(KEY_LAST_CP_TICKERS_CALL_TIME, 0L)
    fun saveLastCpTickersCallTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_LAST_CP_TICKERS_CALL_TIME, time).apply()
    }

    // Last time the app database was updated
    fun getLastAppUpdateTime(): Long = preference.getLong(KEY_LAST_APP_UPDATE_TIME, 0L)
    fun setLastAppUpdateTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_LAST_APP_UPDATE_TIME, time).apply()
    }

    // Constants
    companion object {
        private const val SETTINGS = "settings"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LAST_TOP_MOVERS_CALL_TIME = "last_top_movers_time"
        private const val KEY_LAST_CP_TICKERS_CALL_TIME = "last_cp_tickers_time"
        private const val KEY_LAST_APP_UPDATE_TIME = "last_app_update_time"
    }
}