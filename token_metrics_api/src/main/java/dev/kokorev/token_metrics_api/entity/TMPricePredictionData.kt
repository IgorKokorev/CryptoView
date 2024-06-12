package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class TMPricePredictionData (
    
    @SerializedName("TOKEN_ID"                  ) var tokenId               : Int?                   = null,
    @SerializedName("TOKEN_NAME"                ) var tokenName             : String?                = null,
    @SerializedName("DATE"                      ) var date                  : String?                = null,
    @SerializedName("FORECASTS_FOR_NEXT_7_DAYS" ) var forecastForNext7Days  : Map<String, Forecast> = emptyMap(),
    @SerializedName("PREDICTED_RETURNS_7D"      ) var predictedReturns7d    : Double?                = null

)



data class Forecast (
    @SerializedName("forecast"       ) var forecast      : Double? = null,
    @SerializedName("forecast_lower" ) var forecastLower : Double? = null,
    @SerializedName("forecast_upper" ) var forecastUpper : Double? = null
)