package dev.kokorev.cryptoview.views.fragments

enum class SearchSorting(val str: String) {
    SYMBOL("symbol"),
    NAME("name"),
    PRICE("price"),
    CHANGE24HR("price_change"),
    ATH("ath"),
    ATH_CHANGE("ath_change"),
    VOLUME("volume"),
    MCAP("mcap"),
    RANK("rank"),
    NONE("none");

    companion object {
        private val map = SearchSorting.values().associateBy { it.str }
        infix fun from(str: String?) = map.get(str)
    }
}