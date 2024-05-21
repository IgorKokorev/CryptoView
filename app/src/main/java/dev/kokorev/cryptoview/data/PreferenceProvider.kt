package dev.kokorev.cryptoview.data

import android.content.Context
import android.content.SharedPreferences
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.views.fragments.TickerPriceSorting

// Working with SharedPreferences of the app
class PreferenceProvider(context: Context) {
    // Application context
    private val appContext = context.applicationContext

    // SharedPreferences
    private val preference: SharedPreferences =
        appContext.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE)

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
    fun saveLastAppUpdateTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_LAST_APP_UPDATE_TIME, time).apply()
    }

    // Last time CoinPaprika tickers db was updated
    fun getCPTickersUpdateTime(): Long = preference.getLong(KEY_CP_TICKERS_UPDATE_TIME, 0L)

    fun saveCPTickersUpdateTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_CP_TICKERS_UPDATE_TIME, time).apply()
    }

    // Min mcap of coins to show
    fun getMinMcap(): Long = preference.getLong(KEY_MIN_MCAP, DEFAULT_MIN_MCAP)
    fun saveMinMcap(mcap: Long) {
        if (Constants.minMCaps.contains(mcap))
            preference.edit().putLong(KEY_MIN_MCAP, mcap).apply()
    }

    // Min daily volume of coins to show
    fun getMinVol(): Long = preference.getLong(KEY_MIN_VOL, DEFAULT_MIN_VOL)
    fun saveMinVol(vol: Long) {
        if (Constants.minVols.contains(vol))
            preference.edit().putLong(KEY_MIN_VOL, vol).apply()
    }

    // Main fragment gainers and losers sorting price change period
    fun getMainPriceSorting(): TickerPriceSorting {
        val str = preference.getString(KEY_MAIN_PRICE_SORTING, DEFAULT_MAIN_PRICE_SORTING.str)
        val sorting: TickerPriceSorting =
            (TickerPriceSorting from str) ?: DEFAULT_MAIN_PRICE_SORTING
        return sorting
    }

    fun saveMainPriceSorting(sorting: TickerPriceSorting) {
        preference.edit().putString(KEY_MAIN_PRICE_SORTING, sorting.str).apply()
    }

    // Number of gainers and losers to show on main
    fun getNumGainers(): Int = preference.getInt(KEY_NUM_GAINERS, DEFAULT_NUM_GAINERS)
    fun saveNumGainers(num: Int) {
        if (num in 1..20)
            preference.edit().putInt(KEY_NUM_GAINERS, num).apply()
    }

    // Constants
    companion object {
        private const val SETTINGS = "settings"
        private const val KEY_FIRST_LAUNCH = "first_launch"

        private const val KEY_LAST_TOP_MOVERS_CALL_TIME = "last_top_movers_time"
        private const val KEY_LAST_CP_TICKERS_CALL_TIME = "last_cp_tickers_time"
        private const val KEY_LAST_APP_UPDATE_TIME = "last_app_update_time"
        private const val KEY_CP_TICKERS_UPDATE_TIME = "cp_tickers_update_time"

        private const val KEY_MIN_MCAP = "min_mcap"
        private const val DEFAULT_MIN_MCAP = 10_000_000L
        private const val KEY_MIN_VOL = "min_vol"
        private const val DEFAULT_MIN_VOL = 1_000_000L

        private const val KEY_MAIN_PRICE_SORTING = "main_price_sorting"
        private val DEFAULT_MAIN_PRICE_SORTING = TickerPriceSorting.H24

        private const val KEY_NUM_GAINERS = "number_of_gainers"
        private const val DEFAULT_NUM_GAINERS = 10
    }
}