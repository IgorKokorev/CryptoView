package dev.kokorev.binance_api.entity

import com.google.gson.annotations.SerializedName

enum class BinanceKLineInterval {
    @SerializedName("1s")
    SECOND,

    @SerializedName("1m")
    MINUTE,

    @SerializedName("3m")
    MINUTE3,

    @SerializedName("5m")
    MINUTE5,

    @SerializedName("15m")
    MINUTE15,

    @SerializedName("30m")
    MINUTE30,

    @SerializedName("1h")
    HOUR,

    @SerializedName("2h")
    HOUR2,

    @SerializedName("4h")
    HOUR4,

    @SerializedName("6h")
    HOUR6,

    @SerializedName("8h")
    HOUR8,

    @SerializedName("12h")
    HOUR12,

    @SerializedName("1d")
    DAY,

    @SerializedName("3d")
    DAY3,

    @SerializedName("1w")
    WEEK,

    @SerializedName("1M")
    MONTH
}