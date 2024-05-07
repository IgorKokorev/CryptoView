package dev.kokorev.cmc_api.entity.cmc_metadata

import com.google.gson.annotations.SerializedName

data class CmcCoinDataDTO(
    @SerializedName("id") var id: Long,
    @SerializedName("name") var name: String,
    @SerializedName("symbol") var symbol: String,
    @SerializedName("category") var category: String,
    @SerializedName("description") var description: String,
    @SerializedName("slug") var slug: String,
    @SerializedName("logo") var logo: String,
    @SerializedName("subreddit") var subreddit: String,
    @SerializedName("notice") var notice: String,
    @SerializedName("tags") var tags: ArrayList<String> = arrayListOf(),
    @SerializedName("tag-names") var tagNames: ArrayList<String> = arrayListOf(),
    @SerializedName("tag-groups") var tagGroups: ArrayList<String> = arrayListOf(),
    @SerializedName("urls") var urls: CmcUrls,
    @SerializedName("platform") var platform: CmcPlatform? = null,
    @SerializedName("date_added") var dateAdded: String,
    @SerializedName("twitter_username") var twitterUsername: String,
    @SerializedName("is_hidden") var isHidden: Int,
    @SerializedName("date_launched") var dateLaunched: String,
    @SerializedName("contract_address") var contractAddress: ArrayList<CmcContractAddress> = arrayListOf(),
    @SerializedName("self_reported_circulating_supply") var selfReportedCirculatingSupply: Double? = null,
    @SerializedName("self_reported_tags") var selfReportedTags: ArrayList<String> = arrayListOf(),
    @SerializedName("self_reported_market_cap") var selfReportedMarketCap: Double? = null,
    @SerializedName("infinite_supply") var infiniteSupply: Boolean
)