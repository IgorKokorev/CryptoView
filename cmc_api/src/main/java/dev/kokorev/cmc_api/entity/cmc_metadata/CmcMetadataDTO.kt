package dev.kokorev.cmc_api.entity.cmc_metadata

import com.google.gson.annotations.SerializedName
import dev.kokorev.cmc_api.entity.CmcStatus

data class CmcMetadataDTO(
    @SerializedName("status") var status: CmcStatus,
    @SerializedName("data") var data: Map<String, List<CmcCoinDataDTO>>
)
