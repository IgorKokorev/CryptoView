package dev.kokorev.binance_api.entity

import com.google.gson.annotations.SerializedName


enum class Binance24hrStatsType {
    @SerializedName("FULL")
    FULL,

    @SerializedName("MINI")
    MINI
}
