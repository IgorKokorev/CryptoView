package com.coinpaprika.apiclient.entity

import com.google.gson.annotations.SerializedName


data class MoverEntity(
    val id: String,
    val name: String,
    val symbol: String,
    val rank: Int,
    @SerializedName("percent_change") val percentChange: Double
)