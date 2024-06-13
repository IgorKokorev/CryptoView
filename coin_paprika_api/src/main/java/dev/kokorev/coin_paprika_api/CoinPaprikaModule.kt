package dev.kokorev.coin_paprika_api

import com.coinpaprika.apiclient.COINPAPRIKA_BASE_URL
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import dagger.Module
import dagger.Provides
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class CoinPaprikaModule {
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
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val gson = GsonBuilder().registerTypeAdapter(Instant::class.java,
            JsonDeserializer<Instant> { json, _, _ ->
                ZonedDateTime.parse(
                    json.asJsonPrimitive.asString
                ).toInstant()
            }
        ).create()
        
        return Retrofit.Builder()
            .baseUrl(COINPAPRIKA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCoinPaprikaApi(retrofit: Retrofit): CoinPaprikaApi = retrofit.create(CoinPaprikaApi::class.java)
}
