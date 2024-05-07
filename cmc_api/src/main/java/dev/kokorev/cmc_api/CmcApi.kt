package dev.kokorev.cmc_api

import dev.kokorev.cmc_api.entity.cmc_listing.CmcListingDTO
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcMetadataDTO
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CmcApi {
    @GET("/v2/cryptocurrency/info")
    fun getMetadata(
        @Query("symbol") symbol: String
    ) : Observable<CmcMetadataDTO>

    @GET("/v1/cryptocurrency/listings/latest")
    fun getListingLatest(
        @Query("start") start: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("market_cap_min") marketCapMin: Int = 10_000_000,
        @Query("volume_24h_min") volume24hMin: Int = 10_000_000,
        @Query("sort") sort: String = "percent_change_24h",
        @Query("sort_dir") sortDir: String = "desc",
        @Query("cryptocurrency_type") cryptocurrencyType: String = "all"
    ) : Observable<CmcListingDTO>
}