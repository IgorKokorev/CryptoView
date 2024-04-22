package dev.kokorev.binance_api.entity

import com.google.gson.annotations.SerializedName

data class BinanceAvgPriceDTO(
    @SerializedName("closeTime") val closeTime: Long,
    @SerializedName("mins") val mins: Int,
    @SerializedName("price") val price: String
)