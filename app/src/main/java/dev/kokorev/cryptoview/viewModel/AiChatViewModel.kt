package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.MessageDB
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.token_metrics_api.entity.AiAnswer
import dev.kokorev.token_metrics_api.entity.AiQuestion
import dev.kokorev.token_metrics_api.entity.AiQuestionMessage
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class AiChatViewModel : ViewModel() {

    
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository
    
    val messages: Observable<List<MessageDB>>

    init {
        App.instance.dagger.inject(this)
        messages = repository.getNewMessages()
    }
    
    fun saveQuestion(name: String, question: String) = repository.saveQuestion(name, question)
    fun saveAnswer(name: String, answer: String) = repository.saveAnswer(name, answer)
    fun askTokenMetricsAi(question: String): Maybe<AiAnswer> {
        val aiQuestionMessage = AiQuestionMessage(question)
        val aiQuestion = AiQuestion(arrayListOf(aiQuestionMessage))
        return remoteApi.askTokenMetricsAi(aiQuestion)
    }
    fun askGemini(question: String) = remoteApi.askGemini(question)
}