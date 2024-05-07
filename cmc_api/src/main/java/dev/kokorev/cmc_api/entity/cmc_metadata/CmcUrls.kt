package dev.kokorev.cmc_api.entity.cmc_metadata

import com.google.gson.annotations.SerializedName

data class CmcUrls(
    @SerializedName("website") var website: ArrayList<String>,
    @SerializedName("twitter") var twitter: ArrayList<String>,
    @SerializedName("message_board") var messageBoard: ArrayList<String>,
    @SerializedName("chat") var chat: ArrayList<String>,
    @SerializedName("facebook") var facebook: ArrayList<String>,
    @SerializedName("explorer") var explorer: ArrayList<String>,
    @SerializedName("reddit") var reddit: ArrayList<String>,
    @SerializedName("technical_doc") var technicalDoc: ArrayList<String>,
    @SerializedName("source_code") var sourceCode: ArrayList<String>,
    @SerializedName("announcement") var announcement: ArrayList<String>
)