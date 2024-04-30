package dev.kokorev.cmc_api

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

@Module
class CmcModule {

    // Building OkHttpClient with required parameters
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        // Adding logging interceptor
        .addInterceptor(HttpLoggingInterceptor().apply {
            if (BuildConfig.DEBUG) {
                level = HttpLoggingInterceptor.Level.BODY
            }
        })
        // Adding headers interceptor that inserts auth header to every request
        .addInterceptor { chain ->
            val request = chain.request()
            val requestWithHeaders = request.newBuilder()
                .addHeader("Accept", "application/json")
                .addHeader("X-CMC_PRO_API_KEY", CmcApiKey.key)
                .build()
            return@addInterceptor chain.proceed(requestWithHeaders)
        }
        .build()

    // Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(CmcApiConstants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .client(okHttpClient)
        .build()

    @Provides
    @Singleton
    fun provideCmcApi(retrofit: Retrofit): CmcApi = retrofit.create(CmcApi::class.java)
}
