package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class TMMarketMetrics(

    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("length") var length: Int? = null,
    @SerializedName("data") var data: ArrayList<TMMarketMetricsData> = arrayListOf()

)

data class TMMarketMetricsData(

    @SerializedName("DATE") var date: String? = null,
    @SerializedName("TOTAL_CRYPTO_MCAP") var totalCryptoMcap: Double? = null,
    @SerializedName("TM_GRADE_PERC_HIGH_COINS") var tmGradePercHighCoins: Double? = null,
    @SerializedName("TM_GRADE_SIGNAL") var tmGradeSignal: Int? = null,
    @SerializedName("LAST_TM_GRADE_SIGNAL") var lastTmGradeSignal: Int? = null

)