package dev.kokorev.token_metrics_api

import dev.kokorev.token_metrics_api.entity.AiReport
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.time.Instant

interface TokenMetricsApi {
    @GET("ai-reports")
    fun getAiReports(
        @Query("token_id") tokenId: Int? = null,
        @Query("symbol") symbol: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Observable<AiReport>

}