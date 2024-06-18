package dev.kokorev.cryptoview.data.sharedPreferences

import dev.kokorev.cryptoview.views.fragments.MainPriceSorting
import dev.kokorev.cryptoview.views.fragments.SearchSorting

// Constants for SharedPreferences

// name of SharedPreferences
const val SETTINGS = "settings"

// at what time to send daily portfolio notifications
const val KEY_TO_NOTIFY_PORTFOLIO: String = "to_notify_portfolio"
const val KEY_PORTFOLIO_NOTIFICATION_TIME: String = "portfolio_notification_time"
const val DEFAULT_PORTFOLIO_NOTIFICATION_TIME: Int = 9 * 60 // mins from day start
const val PORTFOLIO_NOTIFICATION_TIME_MIN: Int = 0 // in minutes from day start
const val PORTFOLIO_NOTIFICATION_TIME_MAX: Int = 24 * 60 - 1 //  23:59

// do we need to monitor favorites changes and to notify about movements
const val KEY_TO_CHECK_FAVORITES: String = "to_check_favorites"
const val KEY_FAVORITE_CHANGE = "favorite_min_change"
const val DEFAULT_FAVORITE_CHANGE = 5.0f
const val FAVORITE_MIN_CHANGE: Float = 1.0f
const val FAVORITE_MAX_CHANGE: Float = 20.0f

// how many gainers/losers to show
const val KEY_NUM_TOP_COINS = "num_top_coins"
const val DEFAULT_NUM_TOP_COINS = 10
const val TOP_COINS_FROM = 3
const val TOP_COINS_TO = 20

// min market cap for coins to show
const val KEY_MIN_MCAP = "min_mcap"
const val DEFAULT_MIN_MCAP = 10_000_000L
val MIN_MCAPS = listOf<Long>(
        10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L, 250_000_000L, 500_000_000L, 1_000_000_000L, 10_000_000_000L
)   // List of possible "min mcap" values

// min trade volume for coins to show
const val KEY_MIN_VOL = "min_vol"
const val DEFAULT_MIN_VOL = 1_000_000L
val MIN_VOLS = listOf<Long>(
        1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L
)   // List of possible "min mcap" values

// price sorting on main fragment
const val KEY_MAIN_PRICE_SORTING = "main_price_sorting"
val DEFAULT_MAIN_PRICE_SORTING = MainPriceSorting.H24

// coins sorting on search fragment
const val KEY_SEARCH_SORTING = "search_sorting"
val DEFAULT_SEARCH_SORTING = SearchSorting.NONE
const val KEY_SEARCH_SORTING_DIRECTION = "search_sorting_direction"
const val DEFAULT_SEARCH_SORTING_DIRECTION = 1

// TokenMetrics sentiment last request time
const val KEY_TM_SENTIMENT_CALL_TIME = "tm_sentiment_time"
// TokenMetrics market metrics last request time
const val KEY_TM_MARKET_METRICS_CALL_TIME = "tm_market_metrics_time"
// CoinPaprika all tickers last request time
const val KEY_CP_TICKERS_CALL_TIME = "cp_tickers_time"
// Last portfolio evaluation time
const val KEY_PORTFOLIO_EVALUATION_TIME = "portfolio_evaluation_time"
// Last portfolio change calculation time
const val KEY_PORTFOLIO_CHANGE_TIME = "portfolio_change_time"

const val KEY_FIRST_LAUNCH_INSTANT = "first_launch_instant"

const val KEY_INSTANT = "instant"

