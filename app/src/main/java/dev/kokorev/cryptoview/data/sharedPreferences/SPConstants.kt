package dev.kokorev.cryptoview.data.sharedPreferences

import dev.kokorev.cryptoview.views.fragments.MainPriceSorting
import dev.kokorev.cryptoview.views.fragments.SearchSorting

const val SETTINGS = "settings"

const val KEY_PORTFOLIO_NOTIFICATION_TIME: String = "portfolio_notification_time"
const val DEFAULT_PORTFOLIO_NOTIFICATION_TIME: Float = 9.0F
const val PORTFOLIO_NOTIFICATION_TIME_MIN: Float = 0.0F
const val PORTFOLIO_NOTIFICATION_TIME_MAX: Float = 23.5F
const val KEY_TO_NOTIFY_PORTFOLIO: String = "to_notify_portfolio"
const val KEY_TO_CHECK_FAVORITES: String = "to_check_favorites"
const val KEY_TM_SENTIMENT_LAST_CALL_TIME = "last_tm_sentiment_time"
const val KEY_FAVORITE_MIN_CHANGE = "favorite_min_change"
const val DEFAULT_FAVORITE_MIN_CHANGE = 5.0f
const val FAVORITE_CHECK_MIN_CHANGE: Float = 1.0f
const val FAVORITE_CHECK_MAX_CHANGE: Float = 20.0f
const val KEY_NUM_TOP_COINS = "number_of_gainers"
const val TOP_COINS_DEFAULT = 10
const val KEY_SEARCH_SORTING_DIRECTION = "search_sorting_direction"
const val DEFAULT_SEARCH_SORTING_DIRECTION = 1
const val TOP_COINS_FROM = 3
const val TOP_COINS_TO = 20
const val KEY_MIN_MCAP = "min_mcap"
const val DEFAULT_MIN_MCAP = 10_000_000L
const val KEY_MIN_VOL = "min_vol"
const val DEFAULT_MIN_VOL = 1_000_000L
const val KEY_CP_TICKERS_LAST_CALL_TIME = "last_cp_tickers_time"
const val KEY_LAST_PORTFOLIO_EVALUATION_TIME = "last_portfolio_evaluation_time"
val MIN_MCAPS = listOf<Long>(
        10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L, 250_000_000L, 500_000_000L, 1_000_000_000L, 10_000_000_000L
)   // List of possible "min mcap" values
val MIN_VOLS = listOf<Long>(
        1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L
)   // List of possible "min mcap" values
const val KEY_MAIN_PRICE_SORTING = "main_price_sorting"
val DEFAULT_MAIN_PRICE_SORTING = MainPriceSorting.H24
const val KEY_SEARCH_SORTING = "search_sorting"
val DEFAULT_SEARCH_SORTING = SearchSorting.NONE

