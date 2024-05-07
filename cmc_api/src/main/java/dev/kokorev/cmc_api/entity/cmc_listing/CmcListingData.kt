package dev.kokorev.cmc_api.entity.cmc_listing

import com.google.gson.annotations.SerializedName
import okhttp3.internal.platform.Platform

data class CmcListingData(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("symbol") var symbol: String,
    @SerializedName("slug") var slug: String,
    @SerializedName("cmc_rank") var cmcRank: Int,
    @SerializedName("num_market_pairs") var numMarketPairs: Int,
    @SerializedName("circulating_supply") var circulatingSupply: Double,
    @SerializedName("total_supply") var totalSupply: Double,
    @SerializedName("max_supply") var maxSupply: Double,
    @SerializedName("infinite_supply") var infiniteSupply: Boolean,
    @SerializedName("last_updated") var lastUpdated: String,
    @SerializedName("date_added") var dateAdded: String,
    @SerializedName("tags") var tags: ArrayList<String> = arrayListOf(),
    @SerializedName("platform") var platform: CmcListingPlatform? = null,
    @SerializedName("self_reported_circulating_supply") var selfReportedCirculatingSupply: String,
    @SerializedName("self_reported_market_cap") var selfReportedMarketCap: String,
    @SerializedName("quote") var quote: Map<String, CmcListingCoinInfo> = HashMap()
)