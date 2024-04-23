package dev.kokorev.binance_api.entity

import com.google.gson.annotations.SerializedName

data class BinanceExchangeInfoDTO(
    @SerializedName("timezone") val timezone: String,
    @SerializedName("serverTime") val serverTime: Int,
//    @SerializedName("rateLimits") val rateLimits: ArrayList<RateLimit>,
    @SerializedName("exchangeFilters") val exchangeFilters: ArrayList<String>,
    @SerializedName("symbols") val binanceSymbolDTOS: ArrayList<BinanceSymbolDTO>
)
