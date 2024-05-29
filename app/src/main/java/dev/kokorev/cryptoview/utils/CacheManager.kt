package dev.kokorev.cryptoview.utils

import android.content.Context
import android.util.Log
import dev.kokorev.token_metrics_api.entity.TMSentiment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.charset.Charset
import java.time.Instant


class CacheManager(private val context: Context) {
    private val cacheDir = "cache"
    private val tmSentimentCacheFile = "tm_sentiment_cache"
    private val dir = File(context.filesDir, cacheDir).apply {
        if (!exists()) mkdir()
    }

    fun saveTMSentiment(data: TMSentiment) {
        val json = Json.encodeToString(data)
        writeFile(tmSentimentCacheFile, json)
    }

    fun getTMSentiment(): TMSentiment? {
        val sentiment: TMSentiment? =
            try {
                Json.decodeFromString<TMSentiment>(readFile(tmSentimentCacheFile))
            } catch (e: Exception) {
                null
            }
        return sentiment
    }

    fun writeFile(fileName: String, sBody: String) {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { fOut ->
            fOut.write(sBody.toByteArray(Charset.defaultCharset()))
            Log.d(this.javaClass.simpleName, "Instant: ${Instant.now()} writeFile ${fileName} success")
        }
    }

    fun readFile(fileName: String): String {
        val result = StringBuilder()
        context.openFileInput(fileName).use { fIn ->
            fIn.bufferedReader().forEachLine {
                result.append(it)
            }
        }
        val body = result.toString()
        Log.d(this.javaClass.simpleName, "Instant: ${Instant.now()} readFile ${fileName} success, body: ${body}")

        return body
    }
}