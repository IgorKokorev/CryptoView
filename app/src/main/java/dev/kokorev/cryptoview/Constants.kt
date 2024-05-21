package dev.kokorev.cryptoview

object Constants {
    const val COIN_SYMBOL: String = "coin_symbol" // constants to use as key in bundles
    const val COIN_NAME: String = "coin_name"
    const val COIN_PAPRIKA_ID: String = "coin_paprika_id"

    const val APP_UPDATE_INTERVAL: Long = 1000L * 60 * 1 /* minutes */ * 1 /* hours */ * 1 /* days */ // Update application databases interval
    const val CP_TICKERS_UPDATE_INTERVAL: Long = 1000L * 60 * 1 /* minutes */ * 1 /* hours */ * 1 /* days */ // Update cp tickers db interval
    const val BACK_CLICK_TIME_INTERVAL: Long = 1000L * 3 // time in millis between 2 backpresses to exit the app
    const val TOP_MOVERS_CALL_INTERVAL = 1000L * 60 * 1 // 1 min interval before we request top movers again
    const val CHAT_SHOW_TIME: Long = 1000L * 60 * 60 * 24 * 7 // Show in chat only messages not older than

    val minMCaps = listOf<Long>(
        10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L, 10_000_000_000L
    )   // List of possible "min mcap" values
    val minVols = listOf<Long>(
        1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L, 1_000_000_000L
    )   // List of possible "min mcap" values

    const val TOP_COINS_FROM = 3
    const val TOP_COINS_TO = 20
    const val TOP_COINS_DEFAULT = 10

    const val MAIN_FRAGMENT_TAG = "main"
    const val COIN_FRAGMENT_TAG = "coin"
    const val FAVORITES_FRAGMENT_TAG = "favorites"
    const val SEARCH_FRAGMENT_TAG = "search"
    const val CHAT_FRAGMENT_TAG = "chat"
    const val SETTINGS_FRAGMENT_TAG = "settings"

}