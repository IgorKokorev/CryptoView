package dev.kokorev.cryptoview.backgroundService

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import com.coinpaprika.apiclient.entity.PortfolioTransactionDB
import dev.kokorev.coin_paprika_api.entity.OHLCVEntity
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesLong
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.toInstant
import dev.kokorev.cryptoview.utils.toLocalDate
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.DoubleAdder
import javax.inject.Inject

class PortfolioEvaluationService : Service() {
    @Inject
    lateinit var remoteApi: RemoteApi
    
    @Inject
    lateinit var repository: Repository
    val compositeDisposable = CompositeDisposable()
    
    private var portfolioEvaluationTime: Long by preferencesLong("portfolioEvaluationTime") // the last instant evaluation was made
    private val EVALUATION_INTERVAL: Long = 1000L * 60 /* seconds */ * 60 /* minutes */ * 24 /* hours */
    private val newEvaluationInstantMillisAtomic = AtomicLong(0L) // new evaluation time
    private var newEvaluationTime: Long = 0L
    private val portfolioProcessed: BehaviorSubject<Boolean> =
        BehaviorSubject.create() // receives true if portfolio processed correctly or false in case of errors
    private var newValuation: Double = 0.0
    val atomicNumOfPositions = AtomicInteger(0) // Number of positions already processed
    var totalPositions = 0
    
    private var now = Instant.now()
    private var nowDate = now.toLocalDate()
    private var lastEvaluationInstant = Instant.now()
    private var lastEvaluationDate = lastEvaluationInstant.toLocalDate()
    
    
    init {
        App.instance.dagger.inject(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(
            this.javaClass.simpleName,
            "onCreate"
        )
        // Action when portfolio is evaluated
        val disposable = portfolioProcessed
            .subscribe({
                if (it) {
                    saveNewEvaluation()
                } else {
                    Log.d(
                        this.javaClass.simpleName,
                        "onCreate. portfolioProcessed false state. Something went wrong..."
                    )
                }
            },
                {
                    Log.d(
                        this.javaClass.simpleName,
                        "onCreate. portfolioProcessed error: ${it.localizedMessage}, ${it.stackTrace}"
                    )
                })
        compositeDisposable.add(disposable)
    }
    
    // new valuation is already calculated. to persist it and set new evaluation date
    private fun saveNewEvaluation() {
        val newEvaluationInstant = Instant.ofEpochMilli(newEvaluationTime)
        val disposable = repository.getPortfolioEvaluationByDate(newEvaluationInstant.toLocalDate())
            // If evaluation for the date already exists. It means inflows/outflows were saved
            .doOnSuccess { evaluation ->
                Log.d(
                    this.javaClass.simpleName,
                    "saveNewEvaluation Saving existing evaluation. newValuation = ${newValuation}, time = ${newEvaluationInstant}, date = ${newEvaluationInstant.toLocalDate()}"
                )
                evaluation.valuation = newValuation
                repository.savePortfolioEvaluation(evaluation)
                portfolioEvaluationTime = newEvaluationInstant.toEpochMilli()
            }
            // No evaluation for the date - save a new one
            .doOnComplete {
                Log.d(
                    this.javaClass.simpleName,
                    "saveNewEvaluation creating and saving new evaluation"
                )
                val evaluation = PortfolioEvaluationDB(
                    date = newEvaluationInstant.toLocalDate(),
                    valuation = newValuation,
                    inflow = 0.0
                )
                repository.savePortfolioEvaluation(evaluation)
                portfolioEvaluationTime = newEvaluationInstant.toEpochMilli()
            }
            .doOnError {
                Log.d(
                    this.javaClass.simpleName,
                    "saveNewEvaluation error: ${it.localizedMessage}, ${it.stackTrace}"
                )
            }
            .subscribe()
        compositeDisposable.add(disposable)
    }
    
    override fun onBind(intent: Intent): IBinder {
        // No bind
        Log.d(
            this.javaClass.simpleName,
            "onBind"
        )
        return Binder()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        now = Instant.now()
        nowDate = now.toLocalDate()
        lastEvaluationInstant = Instant.ofEpochMilli(portfolioEvaluationTime)
        lastEvaluationDate = lastEvaluationInstant.toLocalDate()
        Log.d(
            this.javaClass.simpleName,
            "onStartCommand started. Now: ${now}, last evaluation instant: ${lastEvaluationInstant}"
        )
        // Check if time enough pasts since last evaluation
        if (now.toEpochMilli() < portfolioEvaluationTime + EVALUATION_INTERVAL) {
            Log.d(
                this.javaClass.simpleName,
                "onStartCommand. not enough time pasts, stopping"
            )
            return START_NOT_STICKY
        }
        val positionDisposable = repository.getAllPortfolioPositionsSingle()
            .doOnSuccess { positionsNow ->
                Log.d(
                    this.javaClass.simpleName,
                    "onStartCommand. positionsMaybe success, num of positions in portfolio: ${positionsNow.size}"
                )
                checkTransactionsAndEvaluate(positionsNow)
            }
            .doOnError {
                Log.d(
                    this.javaClass.simpleName,
                    "onStartCommand. positionsMaybe error: ${it.localizedMessage}, ${it.stackTrace}"
                )
            }
            .doOnComplete {
                Log.d(this.javaClass.simpleName, "onStartCommand. positionsMaybe empty response")
            }
            .subscribe()
        compositeDisposable.add(positionDisposable)
        Log.d(
            this.javaClass.simpleName,
            "onStartCommand. Finished evaluation process"
        )
        return super.onStartCommand(intent, flags, startId)
    }
    
    // restore the portfolio state at the start of the day and then evaluate
    private fun checkTransactionsAndEvaluate(
        positionsNow: List<PortfolioPositionDB>
    ) {
        val transactionDisposable = repository.findTransactionsFrom(nowDate)
            .doOnSuccess { transactions ->
                Log.d(
                    this.javaClass.simpleName,
                    "onStartCommand. positionsMaybe -> transactionsMaybe success, found transactions: ${transactions.size}"
                )
                val positionsOld = revertTransactions(positionsNow, transactions)
                evaluatePortfolio(positionsOld)
            }
            .doOnError {
                Log.d(
                    this.javaClass.simpleName,
                    "onStartCommand. positionsMaybe -> transactionsMaybe error: ${it.localizedMessage}, ${it.stackTrace}"
                )
            }
            .doOnComplete {
                Log.d(
                    this.javaClass.simpleName,
                    "onStartCommand. positionsMaybe -> transactionsMaybe empty: No today's transactions found"
                )
                evaluatePortfolio(positionsNow)
            }
            .subscribe()
        compositeDisposable.add(transactionDisposable)
    }
    
    // restore portfolio state with given transactions
    private fun revertTransactions(
        positionsNow: List<PortfolioPositionDB>,
        transactions: List<PortfolioTransactionDB>
    ): MutableList<PortfolioPositionDB> {
        val positionsOld = positionsNow.toMutableList()
        // correct positions for every transaction
        transactions.forEach { transaction ->
            val position = positionsOld.find { it.coinPaprikaId == transaction.coinPaprikaId }
            if (position == null) {
                Log.d(
                    this.javaClass.simpleName,
                    "onStartCommand. positionsMaybe -> transactionsMaybe -> revertTransactions. Restoring empty position: ${transaction.coinPaprikaId} qty: ${-transaction.quantity}"
                )
                val oldPosition = PortfolioPositionDB(
                    coinPaprikaId = transaction.coinPaprikaId,
                    quantity = -transaction.quantity,
                    priceLastEvaluation = transaction.price
                )
                positionsOld.add(oldPosition)
            } else {
                Log.d(
                    this.javaClass.simpleName,
                    "onStartCommand. positionsMaybe -> transactionsMaybe -> revertTransactions. Correcting position: ${transaction.coinPaprikaId} qty: ${-transaction.quantity}"
                )
                position.quantity -= transaction.quantity
            }
        }
        return positionsOld
    }
    
    // Calculate valuation of given portfolio. Get last prices for each position
    private fun evaluatePortfolio(positions: List<PortfolioPositionDB>) {
        val valuationAdder = DoubleAdder() // Valuation of the portfolio
        newEvaluationInstantMillisAtomic.set(0L) // Evaluation time in millis from remote API
        
        atomicNumOfPositions.set(0) // Number of positions already processed
        totalPositions = positions.size
        
        // Empty portfolio -> successful valuation = 0
        if (totalPositions == 0) {
            newValuation = 0.0
            newEvaluationTime = nowDate.toInstant().minusSeconds(1).toEpochMilli()
            portfolioProcessed.onNext(true)
            return
        }
        
        for (position in positions) {
            val disposable = remoteApi.getCoinPaprikaOhlcvLatest(position.coinPaprikaId)
                .doOnSuccess { list ->
                    if (list.isNotEmpty()) {
                        val ohlcv = list.maxByOrNull { it.timeClose }
                        if (ohlcv != null) {
                            valuationAdder.add(evaluatePosition(ohlcv, position, valuationAdder))
                            if (atomicNumOfPositions.get() == totalPositions) {
                                newValuation = valuationAdder.sum()
                                newEvaluationTime = newEvaluationInstantMillisAtomic.get()
                                portfolioProcessed.onNext(true)
                            }
                        } else {
                            // no price info
                            portfolioProcessed.onNext(false)
                        }
                    } else {
                        // empty response
                        portfolioProcessed.onNext(false)
                    }
                }
                .doOnError {
                    Log.d(
                        this.javaClass.simpleName,
                        "onStartCommand. evaluatePortfolio. Error: ${it.localizedMessage}, ${it.stackTrace}"
                    )
                }
                .subscribe()
            compositeDisposable.add(disposable)
        }
    }
    
    private fun evaluatePosition(
        ohlcv: OHLCVEntity,
        position: PortfolioPositionDB,
        valuationAdder: DoubleAdder,
    ): Double {
        val ohlcvTime = ohlcv.timeClose.toEpochMilli()
        val savedEvaluationTime = newEvaluationInstantMillisAtomic.get()
        if ((savedEvaluationTime != 0L) and (savedEvaluationTime != ohlcvTime)) {
            Log.d(
                this.javaClass.simpleName,
                "onStartCommand. evaluatePortfolio. TimeClose for different positions differs. Saved time: ${savedEvaluationTime}, time for ${position.coinPaprikaId}: ${ohlcvTime}"
            )
            portfolioProcessed.onNext(false)
            return 0.0
        } else {
            newEvaluationInstantMillisAtomic.set(ohlcvTime)
            val positionsProcessed = atomicNumOfPositions.incrementAndGet()
            Log.d(
                this.javaClass.simpleName,
                "onStartCommand. evaluatePortfolio. Successfully added position valuation for ${position.coinPaprikaId}. Processed ${positionsProcessed} positions"
            )
            return position.quantity * ohlcv.close
        }
    }
    
    override fun onLowMemory() {
        Log.d(
            this.javaClass.simpleName,
            "onLowMemory"
        )
        super.onLowMemory()
    }
    override fun onDestroy() {
        Log.d(
            this.javaClass.simpleName,
            "onDestroy"
        )
        compositeDisposable.clear()
        super.onDestroy()
    }
}
