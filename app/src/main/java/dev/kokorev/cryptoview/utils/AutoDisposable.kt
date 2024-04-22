package dev.kokorev.cryptoview.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

class AutoDisposable : DefaultLifecycleObserver {
    lateinit var compositeDisposable: CompositeDisposable

    fun bindTo(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
        compositeDisposable = CompositeDisposable()
    }

    fun add(disposable: Disposable) {
        if (::compositeDisposable.isInitialized) {
            compositeDisposable.add(disposable)
        } else {
            throw NotImplementedError("must bind AutoDisposable to a Lifecycle first")
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        compositeDisposable.dispose()
        super.onDestroy(owner)
    }
}

fun Disposable.addTo(autoDisposable: AutoDisposable) {
    autoDisposable.add(this)
}