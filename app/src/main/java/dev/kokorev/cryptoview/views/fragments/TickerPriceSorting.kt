package dev.kokorev.cryptoview.views.fragments

enum class TickerPriceSorting(val str: String) {
    H1("1h"),
    H24("24h"),
    D7("7d"),
    D30("30d"),
    Y1("1y"),
    ATH("ath");

    companion object {
        private val map = TickerPriceSorting.values().associateBy { it.str }
        infix fun from(str: String?) = map.get(str)
    }
}