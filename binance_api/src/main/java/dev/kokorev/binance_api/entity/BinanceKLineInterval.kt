package dev.kokorev.binance_api.entity

import com.google.gson.annotations.SerializedName

enum class BinanceKLineInterval(val value: String) {
    @SerializedName("1s")
    SECOND("1s"),

    @SerializedName("1m")
    MINUTE("1m"),

    @SerializedName("3m")
    MINUTE3("3m"),

    @SerializedName("5m")
    MINUTE5("5m"),

    @SerializedName("15m")
    MINUTE15("15m"),

    @SerializedName("30m")
    MINUTE30("30m"),

    @SerializedName("1h")
    HOUR("1h"),

    @SerializedName("2h")
    HOUR2("2h"),

    @SerializedName("4h")
    HOUR4("4h"),

    @SerializedName("6h")
    HOUR6("6h"),

    @SerializedName("8h")
    HOUR8("8h"),

    @SerializedName("12h")
    HOUR12("12h"),

    @SerializedName("1d")
    DAY("1d"),

    @SerializedName("3d")
    DAY3("3d"),

    @SerializedName("1w")
    WEEK("1w"),

    @SerializedName("1M")
    MONTH("1M")
}

fun BinanceKLineInterval.list(): List<BinanceKLineInterval> = listOf(
    BinanceKLineInterval.SECOND,
    BinanceKLineInterval.MINUTE,
    BinanceKLineInterval.MINUTE3,
    BinanceKLineInterval.MINUTE5,
    BinanceKLineInterval.MINUTE15,
    BinanceKLineInterval.MINUTE30,
    BinanceKLineInterval.HOUR,
    BinanceKLineInterval.HOUR2,
    BinanceKLineInterval.HOUR4,
    BinanceKLineInterval.HOUR6,
    BinanceKLineInterval.HOUR8,
    BinanceKLineInterval.HOUR12,
    BinanceKLineInterval.DAY,
    BinanceKLineInterval.DAY3,
    BinanceKLineInterval.WEEK,
    BinanceKLineInterval.MONTH,
)