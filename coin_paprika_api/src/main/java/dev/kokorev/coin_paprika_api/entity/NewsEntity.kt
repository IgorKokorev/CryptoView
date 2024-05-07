package com.coinpaprika.apiclient.entity

import com.google.gson.annotations.SerializedName


data class NewsEntity(
    val title: String,
    val lead: String,
    val url: String,
    @SerializedName("news_date") val newsDate: String
)