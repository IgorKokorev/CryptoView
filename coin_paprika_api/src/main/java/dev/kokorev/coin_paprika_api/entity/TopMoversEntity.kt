package com.coinpaprika.apiclient.entity


data class TopMoversEntity(
    val gainers: List<MoverEntity>,
    val losers: List<MoverEntity>
)