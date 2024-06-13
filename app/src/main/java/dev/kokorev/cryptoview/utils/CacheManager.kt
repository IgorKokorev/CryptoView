package dev.kokorev.cryptoview.utils

import android.content.Context
import android.util.Log
import dev.kokorev.token_metrics_api.entity.TMMarketMetrics
import dev.kokorev.token_metrics_api.entity.TMSentiment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.rx3.rxMaybe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.charset.Charset
import java.time.Instant
import java.util.concurrent.Executors


class CacheManager(private val context: Context) {
    private val cacheDir = "cache"
    private val tmSentimentCacheFile = "tm_sentiment_cache"
    private val tmMarketMetricsCacheFile = "tm_market_metrics_cache"
    
    private val dir = File(context.filesDir, cacheDir).apply {
        if (!exists()) mkdir()
    }
    
    fun saveTMMarketMetrics(data: ArrayList<TMMarketMetrics>) {
        val json = Json.encodeToString(data)
        writeFile(tmMarketMetricsCacheFile, json)
    }
    
    fun getTMMarketMetrics(): Maybe<ArrayList<TMMarketMetrics>> {
        return readFile(tmMarketMetricsCacheFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                Json.decodeFromString<ArrayList<TMMarketMetrics>>(it)
            }
        
    }
    
    fun saveTMSentiment(data: TMSentiment) {
        val json = Json.encodeToString(data)
        writeFile(tmSentimentCacheFile, json)
    }
    
    fun getTMSentiment(): Maybe<TMSentiment> {
        return readFile(tmSentimentCacheFile)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                Json.decodeFromString<TMSentiment>(it)
            }
    }
    
    fun writeFile(fileName: String, sBody: String) {
        Executors.newSingleThreadExecutor().execute {
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use { fOut ->
                fOut.write(sBody.toByteArray(Charset.defaultCharset()))
                Log.d(this.javaClass.simpleName, "Instant: ${Instant.now()} writeFile ${fileName} success")
            }
        }
    }
    
    fun readFile(fileName: String): Maybe<String> {
        return rxMaybe {
            val result = StringBuilder()
            context.openFileInput(fileName).use { fIn ->
                fIn.bufferedReader().forEachLine {
                    result.append(it)
                }
            }
            val body = result.toString()
            Log.d(this.javaClass.simpleName, "Instant: ${Instant.now()} readFile ${fileName} success, body: ${body}")
            
            body
        }
        
    }
    
}