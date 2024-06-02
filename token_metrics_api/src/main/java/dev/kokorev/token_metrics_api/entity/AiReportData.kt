package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class AiReportData(
    @SerializedName("TOKEN_ID") var tokenId: Int,
    @SerializedName("TOKEN_NAME") var tokenName: String,
    @SerializedName("SYMBOL") var symbol: String,
    @SerializedName("TRADER_REPORT") var traderReport: String? = null,
    @SerializedName("FUNDAMENTAL_REPORT") var fundamentalReport: String? = null,
    @SerializedName("TECHNOLOGY_REPORT") var technologyReport: String? = null
)