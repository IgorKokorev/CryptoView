package com.coinpaprika.apiclient.entity

import com.google.gson.annotations.SerializedName


data class PersonEntity(
    val id: String,
    val name: String,
    val description: String?,
    @SerializedName("teams_count") val teamMembers: Int,
    val links: PersonLinks?,
    val positions: List<PositionEntity>?
)