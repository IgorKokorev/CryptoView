package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class TMPricePredictionData (

    @SerializedName("TOKEN_ID"                  ) var tokenId               : Int?                   = null,
    @SerializedName("TOKEN_NAME"                ) var tokenName             : String?                = null,
    @SerializedName("DATE"                      ) var date                  : String?                = null,
    @SerializedName("FORECASTS_FOR_NEXT_7_DAYS" ) var ForecastForNext7Days : ForecastForNext7Days? = ForecastForNext7Days(),
    @SerializedName("PREDICTED_RETURNS_7D"      ) var predictedReturns7d    : Double?                = null

)



data class ForecastForNext7Days (

    @SerializedName("1-day-forecast" ) var day1Forecast : Double? = null,
    @SerializedName("2-day-forecast" ) var day2Forecast : Double? = null,
    @SerializedName("3-day-forecast" ) var day3Forecast : Double? = null,
    @SerializedName("4-day-forecast" ) var day4Forecast : Double? = null,
    @SerializedName("5-day-forecast" ) var day5Forecast : Double? = null,
    @SerializedName("6-day-forecast" ) var day6Forecast : Double? = null,
    @SerializedName("7-day-forecast" ) var day7Forecast : Double? = null

)