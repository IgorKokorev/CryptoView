package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class AiQuestion(
    @SerializedName("messages" ) var messages : ArrayList<AiQuestionMessage> = arrayListOf()
)
