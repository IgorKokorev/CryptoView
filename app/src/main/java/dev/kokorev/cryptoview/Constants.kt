package dev.kokorev.cryptoview

object Constants {
    const val SYMBOL: String = "symbol"
    const val ID: String = "id"
    const val FRAGMENT_TAG = "fragment"
    const val TOP_MOVERS_CALL_INTERVAL = 1000L * 60 * 1 // 1 min interval before we request top movers again
    const val CP_TICKERS_CALL_INTERVAL: Long = 1000L * 60 * 1
    const val MIN_VOLUME = 10_000_000.0
    const val MIN_MCAP = 10_000_000.0

}