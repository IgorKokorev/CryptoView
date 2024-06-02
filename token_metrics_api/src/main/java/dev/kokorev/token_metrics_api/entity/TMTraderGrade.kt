package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class TMTraderGrade(
    @SerializedName("TOKEN_ID"                       ) var tokenId                   : Int?    = null,
    @SerializedName("TOKEN_NAME"                     ) var tokenName                 : String? = null,
    @SerializedName("DATE"                           ) var date                      : String? = null,
    @SerializedName("TA_GRADE"                       ) var taGrade                   : Double? = null,
    @SerializedName("QUANT_GRADE"                    ) var quantGrade                : Double? = null,
    @SerializedName("TM_TRADER_GRADE"                ) var tmTraderGrade             : Double? = null,
    @SerializedName("TM_TRADER_GRADE_24H_PCT_CHANGE" ) var tmTraderGrade24hPctChange : Double? = null
)
