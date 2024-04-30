package com.coinpaprika.apiclient.entity


data class SearchEntity(
    val currencies: List<CoinEntity>,
    val icos: List<IcosEntity>,
    val people: List<TeamMemberEntity>,
    val tags: List<TagEntity>
)