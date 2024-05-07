package dev.kokorev.cmc_api.entity.cmc_metadata

import com.google.gson.annotations.SerializedName
import okhttp3.internal.platform.Platform

data class CmcContractAddress(

    @SerializedName("contract_address" ) var contractAddress : String?   = null,
    @SerializedName("platform"         ) var platform        : CmcContractAddressPlatform? = null

)