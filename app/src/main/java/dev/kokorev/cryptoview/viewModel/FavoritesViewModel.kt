package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.FavoriteCoin
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

class FavoritesViewModel : ViewModel() {
    @Inject
    lateinit var repository: Repository
    private var disposable: Disposable? = null
    val favorites: Observable<List<FavoriteCoin>>

    init {
        App.instance.dagger.inject(this)
        favorites = repository.getFavoriteCoins()
    }
    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}