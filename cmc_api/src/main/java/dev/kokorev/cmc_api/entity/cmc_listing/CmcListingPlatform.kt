package dev.kokorev.cmc_api.entity.cmc_listing

import com.google.gson.annotations.SerializedName

data class CmcListingPlatform (

    @SerializedName("id"            ) var id           : Int?    = null,
    @SerializedName("name"          ) var name         : String? = null,
    @SerializedName("symbol"        ) var symbol       : String? = null,
    @SerializedName("slug"          ) var slug         : String? = null,
    @SerializedName("token_address" ) var tokenAddress : String? = null

)