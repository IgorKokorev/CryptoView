package dev.kokorev.binance_api.entity

import com.google.gson.annotations.SerializedName

class Binance24hrStatsDTO(
    @SerializedName("symbol") val symbol: String,
    @SerializedName("priceChange") val priceChange: String?,
    @SerializedName("priceChangePercent") val priceChangePercent: String?,
    @SerializedName("weightedAvgPrice") val weightedAvgPrice: String?,
    @SerializedName("prevClosePrice") val prevClosePrice: String?,
    @SerializedName("lastPrice") val lastPrice: String,
    @SerializedName("lastQty") val lastQty: String?,
    @SerializedName("bidPrice") val bidPrice: String?,
    @SerializedName("bidQty") val bidQty: String?,
    @SerializedName("askPrice") val askPrice: String?,
    @SerializedName("askQty") val askQty: String?,
    @SerializedName("openPrice") val openPrice: String,
    @SerializedName("highPrice") val highPrice: String,
    @SerializedName("lowPrice") val lowPrice: String,
    @SerializedName("volume") val volume: String,
    @SerializedName("quoteVolume") val quoteVolume: String,
    @SerializedName("openTime") val openTime: Long,
    @SerializedName("closeTime") val closeTime: Long,
    @SerializedName("firstId") val firstId: Int,
    @SerializedName("lastId") val lastId: Int,
    @SerializedName("count") val count: Int,
)