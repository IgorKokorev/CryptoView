package dev.kokorev.coin_paprika_api.entity

import com.google.gson.annotations.SerializedName

data class TickerTickEntity (
    @SerializedName("timestamp"  ) var timestamp : String,
    @SerializedName("price"      ) var price     : Double,
    @SerializedName("volume_24h" ) var volume24h : Long,
    @SerializedName("market_cap" ) var marketCap : Long
)