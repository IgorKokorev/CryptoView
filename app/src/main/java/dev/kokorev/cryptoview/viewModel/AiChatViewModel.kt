package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.MessageDB
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
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
}