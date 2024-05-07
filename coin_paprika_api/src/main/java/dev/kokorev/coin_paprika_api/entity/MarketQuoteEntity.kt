package com.coinpaprika.apiclient.entity

import com.google.gson.annotations.SerializedName


data class MarketQuoteEntity(
    val price: Double,
    @SerializedName("volume_24h") val dailyVolume: Double,
)