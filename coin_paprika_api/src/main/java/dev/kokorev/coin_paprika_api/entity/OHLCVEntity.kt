package dev.kokorev.coin_paprika_api.entity

import com.google.gson.annotations.SerializedName
import java.time.Instant

data class OHLCVEntity(
    @SerializedName("time_open") var timeOpen: Instant,
    @SerializedName("time_close") var timeClose: Instant,
    @SerializedName("open") var open: Double,
    @SerializedName("high") var high: Double,
    @SerializedName("low") var low: Double,
    @SerializedName("close") var close: Double,
    @SerializedName("volume") var volume: Double,
    @SerializedName("market_cap") var marketCap: Double
)
