package dev.kokorev.cryptoview.data

import android.content.Context
import android.content.SharedPreferences
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.views.fragments.MainPriceSorting
import dev.kokorev.cryptoview.views.fragments.SearchSorting
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


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
        preference.edit().putLong(KEY_TOP_MOVERS_LAST_CALL_TIME, time).apply()
    }

    fun getLastTopMoversCallTime(): Long = preference.getLong(KEY_TOP_MOVERS_LAST_CALL_TIME, 0L)

    // Last time in millis when remote API was accessed to get all CoinPaprika tickers
    fun getLastCpTickersCallTime(): Long = preference.getLong(KEY_CP_TICKERS_LAST_CALL_TIME, 0L)
    fun saveLastCpTickersCallTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_CP_TICKERS_LAST_CALL_TIME, time).apply()
    }

    // Last time the app database was updated
    fun getLastAppUpdateTime(): Long = preference.getLong(KEY_APP_LAST_UPDATE_TIME, 0L)
    fun saveLastAppUpdateTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_APP_LAST_UPDATE_TIME, time).apply()
    }

    // Last time CoinPaprika tickers db was updated
    fun getCPTickersUpdateTime(): Long = preference.getLong(KEY_CP_TICKERS_LAST_UPDATE_TIME, 0L)

    fun saveCPTickersUpdateTime() {
        val time = System.currentTimeMillis()
        preference.edit().putLong(KEY_CP_TICKERS_LAST_UPDATE_TIME, time).apply()
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
    fun getMainPriceSorting(): MainPriceSorting {
        val str = preference.getString(KEY_MAIN_PRICE_SORTING, DEFAULT_MAIN_PRICE_SORTING.str)
        val sorting: MainPriceSorting =
            (MainPriceSorting from str) ?: DEFAULT_MAIN_PRICE_SORTING
        return sorting
    }

    fun saveMainPriceSorting(sorting: MainPriceSorting) {
        preference.edit().putString(KEY_MAIN_PRICE_SORTING, sorting.str).apply()
    }

    // Number of gainers and losers to show on main
    fun getNumTopCoins(): Int = preference.getInt(KEY_NUM_TOP_COINS, Constants.TOP_COINS_DEFAULT)
    fun saveNumTopCoins(num: Int) {
        if (num in Constants.TOP_COINS_FROM.. Constants.TOP_COINS_TO)
            preference.edit().putInt(KEY_NUM_TOP_COINS, num).apply()
    }

    // Does user want to get notifications about his favorites changes
    fun toCheckFavorites(): Boolean {
        return preference.getBoolean(KEY_TO_CHECK_FAVORITES, true)
    }

    fun saveCheckFaforites(toCheck: Boolean) {
        preference.edit().putBoolean(KEY_TO_CHECK_FAVORITES, toCheck).apply()
    }

    // Favorites min change in % to send notifications
    fun getFavoriteMinChange(): Float =
        preference.getFloat(KEY_FAVORITE_MIN_CHANGE, DEFAULT_FAVORITE_MIN_CHANGE)

    fun saveFavoriteMinChange(change: Float) {
        if (change in Constants.FAVORITE_CHECK_MIN_CHANGE.. Constants.FAVORITE_CHECK_MAX_CHANGE)
            preference.edit().putFloat(KEY_FAVORITE_MIN_CHANGE, change).apply()
    }

    fun getSearchSorting(): SearchSorting {
        val str = preference.getString(KEY_SEARCH_SORTING, DEFAULT_SEARCH_SORTING.str)
        val sorting: SearchSorting =
            (SearchSorting from str) ?: DEFAULT_SEARCH_SORTING
        return sorting
    }

    fun saveSearchSorting(sorting: SearchSorting) {
        preference.edit().putString(KEY_SEARCH_SORTING, sorting.str).apply()
    }

    fun getSearchSortingDirection(): Int =
        preference.getInt(KEY_SEARCH_SORTING_DIRECTION, DEFAULT_SEARCH_SORTING_DIRECTION)

    fun saveSearchSortingDirection(direction: Int) {
        if (direction != 0) preference.edit().putInt(KEY_SEARCH_SORTING_DIRECTION, direction)
            .apply()
    }


    // last sentiment saved time
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    fun getTMSentimentLastCall(): LocalDateTime {
        val timeStr = preference.getString(KEY_TM_SENTIMENT_LAST_CALL_TIME, "2000-01-01 00:00")
        return LocalDateTime.parse(timeStr, dateTimeFormatter)
    }

    fun saveTMSentimentLastCall(time: LocalDateTime) {
        val timeStr = time.withMinute(0).withSecond(0).withNano(0).format(dateTimeFormatter)
        preference.edit().putString(KEY_TM_SENTIMENT_LAST_CALL_TIME, timeStr).apply()
    }

    // Constants
    companion object {
        private const val SETTINGS = "settings"
        private const val KEY_FIRST_LAUNCH = "first_launch"

        private const val KEY_TOP_MOVERS_LAST_CALL_TIME = "last_top_movers_time"
        const val KEY_CP_TICKERS_LAST_CALL_TIME = "last_cp_tickers_time"
        private const val KEY_APP_LAST_UPDATE_TIME = "last_app_update_time"
        private const val KEY_CP_TICKERS_LAST_UPDATE_TIME = "cp_tickers_update_time"
        const val KEY_TM_SENTIMENT_LAST_CALL_TIME = "last_tm_sentiment_time"

        const val KEY_MIN_MCAP = "min_mcap"
        const val DEFAULT_MIN_MCAP = 10_000_000L
        const val KEY_MIN_VOL = "min_vol"
        const val DEFAULT_MIN_VOL = 1_000_000L

        const val KEY_MAIN_PRICE_SORTING = "main_price_sorting"
        val DEFAULT_MAIN_PRICE_SORTING = MainPriceSorting.H24

        const val KEY_NUM_TOP_COINS = "number_of_gainers"

        const val KEY_TO_CHECK_FAVORITES: String = "to_check_favorites"
        const val KEY_FAVORITE_MIN_CHANGE = "favorite_min_change"
        const val DEFAULT_FAVORITE_MIN_CHANGE = 5.0f

        const val KEY_SEARCH_SORTING = "search_sorting"
        val DEFAULT_SEARCH_SORTING = SearchSorting.NONE
        const val KEY_SEARCH_SORTING_DIRECTION = "search_sorting_direction"
        const val DEFAULT_SEARCH_SORTING_DIRECTION = 1
    }
}
