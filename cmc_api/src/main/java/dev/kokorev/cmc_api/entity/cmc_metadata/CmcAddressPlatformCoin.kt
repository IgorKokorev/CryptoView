package dev.kokorev.cmc_api.entity.cmc_metadata

import com.google.gson.annotations.SerializedName

data class CmcAddressPlatformCoin(

    @SerializedName("id"     ) var id     : String? = null,
    @SerializedName("name"   ) var name   : String? = null,
    @SerializedName("symbol" ) var symbol : String? = null,
    @SerializedName("slug"   ) var slug   : String? = null

)