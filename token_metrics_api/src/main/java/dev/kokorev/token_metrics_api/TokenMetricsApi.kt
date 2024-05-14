package dev.kokorev.token_metrics_api

import dev.kokorev.token_metrics_api.entity.AiAnswer
import dev.kokorev.token_metrics_api.entity.AiQuestion
import dev.kokorev.token_metrics_api.entity.AiReport
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TokenMetricsApi {
    @GET("ai-reports")
    fun getAiReports(
        @Query("token_id") tokenId: Int? = null,
        @Query("symbol") symbol: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Observable<AiReport>

    @POST("tmai")
    fun aiQuestion(
        @Body aiQuestion: AiQuestion
    ) : Observable<AiAnswer>

}