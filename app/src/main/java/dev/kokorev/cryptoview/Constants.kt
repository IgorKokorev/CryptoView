package dev.kokorev.cryptoview

object Constants {
    const val APP_UPDATE_INTERVAL: Long = 1000L * 60 * 1 /* minutes */ * 1 /* hours */ * 1 /* days */ // Update application databases interval
    const val BACK_CLICK_TIME_INTERVAL: Long = 1000L * 3 // time in millis between 2 backpresses to exit the app
    const val COIN_SYMBOL: String = "coin_symbol"
    const val COIN_NAME: String = "coin_name"
    const val COIN_PAPRIKA_ID: String = "coin_paprika_id"
    const val TOP_MOVERS_CALL_INTERVAL = 1000L * 60 * 1 // 1 min interval before we request top movers again
    const val CP_TICKERS_CALL_INTERVAL: Long = 1000L * 60 * 1
    const val MIN_VOLUME = 10_000_000.0
    const val MIN_MCAP = 10_000_000.0
    const val MAIN_FRAGMENT_TAG = "main"
    const val COIN_FRAGMENT_TAG = "coin"
    const val FAVORITES_FRAGMENT_TAG = "favorites"
    const val SEARCH_FRAGMENT_TAG = "search"
    const val SETTINGS_FRAGMENT_TAG = "settings"

}