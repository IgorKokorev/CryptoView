package dev.kokorev.token_metrics_api.entity

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class TMSentiment(

    @SerializedName("DATETIME") var dateTime: String? = null,
    @SerializedName("MARKET_SENTIMENT_GRADE") var marketSentimentGrade: Double? = null,
    @SerializedName("MARKET_SENTIMENT_LABEL") var marketSentimentLabel: String? = null,
    @SerializedName("NEWS_SENTIMENT_GRADE") var newsSentimentGrade: Double? = null,
    @SerializedName("NEWS_SENTIMENT_LABEL") var newsSentimentLabel: String? = null,
    @SerializedName("NEWS_SUMMARY") var newsSummary: String? = null,
    @SerializedName("REDDIT_SENTIMENT_GRADE") var redditSentimentGrade: Double? = null,
    @SerializedName("REDDIT_SENTIMENT_LABEL") var redditSentimentLabel: String? = null,
    @SerializedName("REDDIT_SUMMARY") var redditSummary: String? = null,
    @SerializedName("TWITTER_SENTIMENT_GRADE") var twitterSentimentGrade: Double? = null,
    @SerializedName("TWITTER_SENTIMENT_LABEL") var twitterSentimentLabel: String? = null,
    @SerializedName("TWITTER_SUMMARY") var twitterSummary: String? = null

)