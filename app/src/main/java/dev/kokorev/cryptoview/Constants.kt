package dev.kokorev.cryptoview

const val PORTFOLIO_EVALUATION_SERVICE_ACTION: String = "portfolio_evaluation_action"
const val PORTFOLIO_EVALUATION_SERVICE_REQUEST_ID: Int = 1

object Constants {
    
    const val SETTINGS: String = "settings"
    
    // For Binance Fragment
    const val BINANCE_FIRST_ASSET = "USDT"
    const val BINANCE_SECOND_ASSET = "BTC"
    const val NUM_BARS_TO_SHOW = 100
    const val BINANCE_KLINES_LIMIT: Int = 500
    
    // workers for work manager
    const val TICKER_LOADER_TAG: String = "ticker_loader_tag"
    const val TICKER_LOADER_WORK: String = "ticker_loader_work"
    const val BINANCE_LOADER_TAG: String = "binance_loader_tag"
    const val BINANCE_LOADER_WORK: String = "binance_loader_work"
    
    // working with notifications
    const val NOTIFICATION_PERMISSION_REQUEST_CODE: Int = 1
    const val NOTIFICATION_ID: Int = 1
    const val INTENT_EXTRA_FAVORITE_COIN: String = "intent_extra_favorite_coin"

    // tags to transfer data in bundles
    const val COIN_SYMBOL: String = "coin_symbol" // constants to use as key in bundles
    const val COIN_NAME: String = "coin_name"
    const val COIN_PAPRIKA_ID: String = "coin_paprika_id"

    // Default timings
    const val CP_TICKERS_UPDATE_INTERVAL: Long = 1000L * 60 * 1 /* minutes */ * 1 /* hours */ * 1 /* days */ // Update cp tickers db interval
    const val BACK_CLICK_TIME_INTERVAL: Long = 1000L * 3 // time in millis between 2 backpresses to exit the app
    const val CHAT_SHOW_TIME: Long = 1000L * 60 * 60 * 24 * 7 // Show in chat only messages not older than
    const val INTERVAL_TO_SHOW_FAVORITE_CHANGE = 1000L * 60 * 60 /* minutes */ * 12 /* hours */ * 1 /* days */  // Do not show favorite change notifications more often than

    // Settings defaults


    // Fragments tags for fragment manager
    const val MAIN_FRAGMENT_TAG = "main"
    const val COIN_FRAGMENT_TAG = "coin"
    const val FAVORITES_FRAGMENT_TAG = "favorites"
    const val SEARCH_FRAGMENT_TAG = "search"
    const val CHAT_FRAGMENT_TAG = "chat"
    const val SETTINGS_FRAGMENT_TAG = "settings"
    const val BINANCE_FRAGMENT_TAG: String = "binance"

}