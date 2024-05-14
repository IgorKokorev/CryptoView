package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class AiAnswer(
    @SerializedName("success" ) var success : Boolean?          = null,
    @SerializedName("message" ) var message : String?           = null,
    @SerializedName("answer"  ) var answer  : String?           = null,
    @SerializedName("thread"  ) var thread  : ArrayList<Map<String, String>> = arrayListOf()
)