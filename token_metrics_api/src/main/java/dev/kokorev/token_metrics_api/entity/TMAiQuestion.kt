package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class TMAiQuestion(
    @SerializedName("messages" ) var messages : ArrayList<AiQuestionMessage> = arrayListOf()
)

data class AiQuestionMessage(
    @SerializedName("user" ) var user : String? = null
)