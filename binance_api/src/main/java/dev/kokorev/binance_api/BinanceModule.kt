package dev.kokorev.binance_api

//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import dev.kokorev.binance_api.BuildConfig

@Module
class BinanceModule {
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
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BinanceApiConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideTmdbApi(retrofit: Retrofit): BinanceApi = retrofit.create(BinanceApi::class.java)
}
