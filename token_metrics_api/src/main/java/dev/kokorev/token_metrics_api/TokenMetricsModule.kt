package dev.kokorev.token_metrics_api

import dagger.Module
import dagger.Provides
import dev.kokorev.tocken_metrics_api.BuildConfig
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class TokenMetricsModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        })
        // Adding headers interceptor that inserts auth header to every request
        .addInterceptor { chain ->
            val request = chain.request()
            val requestWithHeaders = request.newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("api_key", TokenMetricsApiKey.key)
                .build()
            return@addInterceptor chain.proceed(requestWithHeaders)
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(TOKEN_METRICS_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideCoinPaprikaApi(retrofit: Retrofit): TokenMetricsApi = retrofit.create(TokenMetricsApi::class.java)
}
