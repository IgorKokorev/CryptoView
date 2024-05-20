package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class AiReport(
    @SerializedName("success") var success: Boolean,
    @SerializedName("message") var message: String,
    @SerializedName("length") var length: Int,
    @SerializedName("data") var data: ArrayList<AiReportData> = arrayListOf()
)

data class AiReportData(
    @SerializedName("TOKEN_ID") var TOKENID: Int,
    @SerializedName("TOKEN_NAME") var TOKENNAME: String,
    @SerializedName("SYMBOL") var SYMBOL: String,
    @SerializedName("TRADER_REPORT") var TRADERREPORT: String? = null,
    @SerializedName("FUNDAMENTAL_REPORT") var FUNDAMENTALREPORT: String? = null,
    @SerializedName("TECHNOLOGY_REPORT") var TECHNOLOGYREPORT: String? = null
)