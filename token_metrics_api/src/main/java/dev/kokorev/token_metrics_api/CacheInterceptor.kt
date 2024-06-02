package dev.kokorev.token_metrics_api

import android.util.Log
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.lang.String
import java.util.concurrent.TimeUnit


class CacheInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        Log.d(this.javaClass.simpleName, "Intercepting request to url: ${request.url}, connection: ${chain.connection()}, headers: ${request.headers}")

        val cacheControl = CacheControl.Builder()
            .maxAge(10, TimeUnit.MINUTES)
            .build()

        val tStart = System.nanoTime()
        val response = chain.proceed(request)
        val tEnd = System.nanoTime()

        Log.d(this.javaClass.simpleName,
            String.format("Received response for %s in %.1fms%n%s", response.request.url, (tEnd - tStart) / 1e6, response.headers)
        )

        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}