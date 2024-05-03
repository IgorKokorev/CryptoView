package dev.kokorev.binance_api.entity

import com.google.gson.annotations.SerializedName

data class BinanceSymbolDTO(
    @SerializedName("symbol") var symbol: String,
    @SerializedName("status") var status: String,
    @SerializedName("baseAsset") var baseAsset: String,
    @SerializedName("baseAssetPrecision") var baseAssetPrecision: Int,
    @SerializedName("quoteAsset") var quoteAsset: String,
    @SerializedName("quotePrecision") var quotePrecision: Int,
    @SerializedName("quoteAssetPrecision") var quoteAssetPrecision: Int,
    @SerializedName("orderTypes") var orderTypes: ArrayList<String>,
    @SerializedName("icebergAllowed") var icebergAllowed: Boolean,
    @SerializedName("ocoAllowed") var ocoAllowed: Boolean,
    @SerializedName("quoteOrderQtyMarketAllowed") var quoteOrderQtyMarketAllowed: Boolean,
    @SerializedName("allowTrailingStop") var allowTrailingStop: Boolean,
    @SerializedName("cancelReplaceAllowed") var cancelReplaceAllowed: Boolean,
    @SerializedName("isSpotTradingAllowed") var isSpotTradingAllowed: Boolean,
    @SerializedName("isMarginTradingAllowed") var isMarginTradingAllowed: Boolean,
    @SerializedName("filters") var filters: ArrayList<Any>,
    @SerializedName("permissions") var permissions: ArrayList<String>,
    @SerializedName("permissionSets") var permissionSets: ArrayList<ArrayList<String>>,
    @SerializedName("defaultSelfTradePreventionMode") var defaultSelfTradePreventionMode: String,
    @SerializedName("allowedSelfTradePreventionModes") var allowedSelfTradePreventionModes: ArrayList<String>
)