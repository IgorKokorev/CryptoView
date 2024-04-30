package dev.kokorev.cmc_api.entity.cmc_listing

import com.google.gson.annotations.SerializedName
import dev.kokorev.cmc_api.entity.CmcStatus

data class CmcListingDTO(
    @SerializedName("data") var data: ArrayList<CmcListingData> = arrayListOf(),
    @SerializedName("status") var status: CmcStatus
)
