package dev.kokorev.cmc_api.entity.cmc_metadata

import com.google.gson.annotations.SerializedName

data class CmcContractAddressPlatform(

    @SerializedName("name" ) var name : String? = null,
    @SerializedName("coin" ) var coin : CmcAddressPlatformCoin?   = null

)