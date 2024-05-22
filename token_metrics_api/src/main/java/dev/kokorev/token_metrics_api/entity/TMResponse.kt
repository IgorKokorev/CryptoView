package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName

data class TMResponse<T>(
    @SerializedName("success") var success: Boolean,
    @SerializedName("message") var message: String,
    @SerializedName("length") var length: Int,
    @SerializedName("data") var data: ArrayList<T> = arrayListOf()
)

