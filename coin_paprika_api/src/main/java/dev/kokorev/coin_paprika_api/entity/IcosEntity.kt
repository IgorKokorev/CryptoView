package com.coinpaprika.apiclient.entity

import com.google.gson.annotations.SerializedName


data class IcosEntity(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("is_new") val isNew: Boolean,
    @SerializedName("rev") val revision: Int
)