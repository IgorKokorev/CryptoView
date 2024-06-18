package dev.kokorev.cryptoview.backgroundService

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import com.coinpaprika.apiclient.entity.PortfolioTransactionDB
import dev.kokorev.coin_paprika_api.entity.OHLCVEntity
import dev.kokorev.cryptoview.addToComposite
import dev.kokorev.cryptoview.appDagger
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_FIRST_LAUNCH_INSTANT
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_PORTFOLIO_CHANGE_TIME
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_PORTFOLIO_EVALUATION_TIME
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInstant
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.utils.toInstant
import dev.kokorev.cryptoview.utils.toLocalDate
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.DoubleAdder
import javax.inject.Inject
import kotlin.math.abs

class PortfolioEvaluationService : Service() {
    @Inject
    lateinit var remoteApi: RemoteApi
    
    @Inject
    lateinit var repository: Repository
    val compositeDisposable = CompositeDisposable()
    
    private var portfolioEvaluationTime: Instant by preferencesInstant(KEY_PORTFOLIO_EVALUATION_TIME) // the last instant evaluation was made
    private var portfolioChangeTime: Instant by preferencesInstant(KEY_PORTFOLIO_CHANGE_TIME) // the last instant evaluation was made
    private var firstLaunchInstant by preferencesInstant(KEY_FIRST_LAUNCH_INSTANT)
    
    private val setPortfolioEvaluationTime: BehaviorSubject<Instant> =
        BehaviorSubject.create() // to correctly set new portfolioEvaluation time
    private val EVALUATION_INTERVAL_SECONDS: Long = 60L /* seconds */ * 60 /* minutes */ * 24 /* hours */
    private val newEvaluationInstantMillisAtomic = AtomicLong(0L) // new evaluation time
    private var newEvaluationTimeMillis: Long = 0L
    private val portfolioProcessed: BehaviorSubject<Boolean> =
        BehaviorSubject.create() // receives true if portfolio processed correctly or false in case of errors
    private var newValuation: Double = 0.0
    val atomicNumOfPositions = AtomicInteger(0) // Number of positions already processed
    var totalPositions = 0
    
    private var now = Instant.now()
    private var nowDate = now.toLocalDate()
    
    //    private var lastEvaluationInstant = Instant.now()
    private var lastEvaluationDate = portfolioEvaluationTime.toLocalDate()
    
    
    init {
        appDagger.inject(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        calculatePortfolioChange(portfolioEvaluationTime)
        
        // Action when portfolio is evaluated
        val disposablePP = portfolioProcessed
            .doOnNext {
                if (it) {
                    saveNewEvaluation()
                } else logd("onCreate. portfolioProcessed false state. Something went wrong...")
            }
            .doOnError {
                logd("onCreate. portfolioProcessed error", it)
            }
            .subscribe()
        compositeDisposable.add(disposablePP)
        
        val disposableSPET = setPortfolioEvaluationTime
            .doOnNext { instant ->
                logd("setPortfolioEvaluationTime, instant = ${instant}")
                portfolioEvaluationTime = instant
                // Check if evaluation change calculated after evaluation is done.
                if (portfolioChangeTime.isBefore(instant)) {
                    calculatePortfolioChange(instant)
                }
            }
            .doOnError {
                logd("setPortfolioEvaluationTime", it)
            }
            .subscribe()
        compositeDisposable.add(disposableSPET)
    }
    
    override fun onBind(intent: Intent): IBinder {
        logd("onBind")
        return Binder()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        now = Instant.now()
        nowDate = now.toLocalDate()
        lastEvaluationDate = portfolioEvaluationTime.toLocalDate()
        logd("onStartCommand started. Now: ${now}, last evaluation instant: ${portfolioEvaluationTime}")
        
        // Check if time enough pasts since last evaluation
        if (now.isBefore(portfolioEvaluationTime.plusSeconds(EVALUATION_INTERVAL_SECONDS))) {
            logd("onStartCommand. not enough time pasts, stopping")
            return START_NOT_STICKY
        } else {
            logd("onStartCommand. Starting portfolio evaluation process")
            startPortfolioEvaluationProcess()
        }
        
        return super.onStartCommand(intent, flags, startId)
    }
    
    private fun startPortfolioEvaluationProcess() {
        val positionDisposable = repository.getAllPortfolioPositionsMaybe()
            .doOnSuccess { positionsNow ->
                logd("onStartCommand. positionsMaybe success, num of positions in portfolio: ${positionsNow.size}")
                checkTransactionsAndEvaluate(positionsNow)
            }
            .doOnError {
                logd("onStartCommand. positionsMaybe error", it)
            }
            .doOnComplete {
                logd("onStartCommand. positionsMaybe empty response")
                checkTransactionsAndEvaluate(emptyList())
            }
            .onErrorComplete()
            .subscribe()
        compositeDisposable.add(positionDisposable)
    }
    
    // restore the portfolio state at the start of the day and then evaluate
    private fun checkTransactionsAndEvaluate(
        positionsNow: List<PortfolioPositionDB>
    ) {
        val transactionDisposable = repository.findTransactionsFrom(nowDate)
            .doOnSuccess { transactions ->
                logd("onStartCommand. positionsMaybe -> transactionsMaybe success, found transactions: ${transactions.size}")
                val positionsOld = revertTransactions(positionsNow, transactions)
                evaluatePortfolio(positionsOld)
            }
            .doOnError {
                logd("onStartCommand. positionsMaybe -> transactionsMaybe", it)
            }
            .doOnComplete {
                logd("onStartCommand. positionsMaybe -> transactionsMaybe empty: No today's transactions found")
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
                logd("onStartCommand. positionsMaybe -> transactionsMaybe -> revertTransactions. Restoring empty position: ${transaction.coinPaprikaId} qty: ${-transaction.quantity}")
                val oldPosition = PortfolioPositionDB(
                    coinPaprikaId = transaction.coinPaprikaId,
                    quantity = -transaction.quantity,
                    priceLastEvaluation = transaction.price
                )
                positionsOld.add(oldPosition)
            } else {
                logd("onStartCommand. positionsMaybe -> transactionsMaybe -> revertTransactions. Correcting position: ${transaction.coinPaprikaId} qty: ${-transaction.quantity}")
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
            newEvaluationTimeMillis = nowDate.toInstant().minusSeconds(1).toEpochMilli()
            portfolioProcessed.onNext(true)
            return
        }
        
        for (position in positions) {
            val disposable = remoteApi.getCoinPaprikaOhlcvLatest(position.coinPaprikaId)
                .doOnSuccess { list ->
                    if (list.isNotEmpty()) {
                        val ohlcv = list.maxByOrNull { it.timeClose }
                        if (ohlcv != null) {
                            valuationAdder.add(evaluatePosition(ohlcv, position))
                            if (atomicNumOfPositions.get() == totalPositions) {
                                newValuation = valuationAdder.sum()
                                newEvaluationTimeMillis = newEvaluationInstantMillisAtomic.get()
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
                    logd("onStartCommand. evaluatePortfolio", it)
                }
                .subscribe()
            compositeDisposable.add(disposable)
        }
    }
    
    private fun evaluatePosition(
        ohlcv: OHLCVEntity,
        position: PortfolioPositionDB,
    ): Double {
        val ohlcvTime = ohlcv.timeClose.toEpochMilli()
        val savedEvaluationTime = newEvaluationInstantMillisAtomic.get()
        if ((savedEvaluationTime != 0L) and (savedEvaluationTime != ohlcvTime)) {
            logd("onStartCommand. evaluatePortfolio. TimeClose for different positions differs. Saved time: ${savedEvaluationTime}, time for ${position.coinPaprikaId}: ${ohlcvTime}")
            portfolioProcessed.onNext(false)
            return 0.0
        } else {
            newEvaluationInstantMillisAtomic.set(ohlcvTime)
            val positionsProcessed = atomicNumOfPositions.incrementAndGet()
            logd("onStartCommand. evaluatePortfolio. Successfully added position valuation for ${position.coinPaprikaId}. Processed ${positionsProcessed} positions")
            return position.quantity * ohlcv.close
        }
    }
    
    // new valuation is already calculated. to persist it and set new evaluation date
    private fun saveNewEvaluation() {
        val newEvaluationInstant = Instant.ofEpochMilli(newEvaluationTimeMillis)
        val disposable = repository.getPortfolioEvaluationByDate(newEvaluationInstant.toLocalDate())
            // If evaluation for the date already exists. It means inflows/outflows were saved
            .doOnSuccess { evaluation ->
                logd("saveNewEvaluation Saving existing evaluation. newValuation = ${newValuation}, time = ${newEvaluationInstant}, date = ${newEvaluationInstant.toLocalDate()}")
                evaluation.valuation = newValuation
                evaluation.positions = totalPositions
                repository.savePortfolioEvaluation(evaluation)
                setPortfolioEvaluationTime.onNext(newEvaluationInstant)
            }
            // No evaluation for the date - save a new one
            .doOnComplete {
                logd("saveNewEvaluation creating and saving new empty evaluation")
                val evaluation = PortfolioEvaluationDB(
                    date = newEvaluationInstant.toLocalDate(),
                    valuation = newValuation,
                    positions = totalPositions,
                    inflow = 0.0
                )
                repository.savePortfolioEvaluation(evaluation)
                setPortfolioEvaluationTime.onNext(newEvaluationInstant)
            }
            .doOnError {
                logd("saveNewEvaluation error", it)
            }
            .subscribe()
        compositeDisposable.add(disposable)
    }
    
    
    private fun calculatePortfolioChange(instant: Instant) {
        repository.getLatestPortfolioEvaluations(portfolioChangeTime.toLocalDate().minusDays(1))
            .doOnSuccess { evaluations ->
                logd("calculatePortfolioChange. Received ${evaluations.size} evaluations from ${portfolioChangeTime.toLocalDate()}")
                evaluations.forEach { evaluation ->
                    logd("calculatePortfolioChange. Not calculated change for evaluation: date: ${evaluation.date}, valuation: ${evaluation.valuation}, inflow: ${evaluation.inflow}, change: ${evaluation.change}, percent = ${evaluation.percentChange}")
                }
                if (evaluations.size < 2) {
                    logd("calculatePortfolioChange. Nothing to calculate")
                    portfolioChangeTime = firstLaunchInstant
                } else {
                    logd("calculatePortfolioChange. Calculating...")
                    portfolioChangeTime = if (performPortfolioChangeCalculation(evaluations)) {
                        repository.saveAllPortfolioEvaluations(evaluations)
                        instant
                    } else firstLaunchInstant
                }
            }
            .doOnError {
                logd("calculatePortfolioChange. Error", it)
            }
            .subscribe()
            .addToComposite(compositeDisposable)
    }
    
    // calculate vakuation changes for given list of valuations
    private fun performPortfolioChangeCalculation(evaluations: List<PortfolioEvaluationDB>): Boolean {
        evaluations[0].run {
            if (change == null || percentChange == null || cumulativeChange == null || cumulativePercentChange == null) {
                logd("Null value for valuation of date: $date. change = $change, percentChange = $percentChange, cumulativeChange = $cumulativeChange, cumulativePercentChange = $cumulativePercentChange")
                return false
            }
        }
        
        for (i in 1 until evaluations.size) {
            if (i == (evaluations.size - 1) && evaluations[i].valuation == null) break
            
            try {
                val valuation = evaluations[i].valuation!!
                val inflow = evaluations[i].inflow!!
                val prevValuation = evaluations[i - 1].valuation!!
                
                val currentChange = valuation - inflow - prevValuation
                
                val percChange = if (prevValuation == 0.0) {
                    if (inflow == 0.0) 0.0
                    else (currentChange / abs(inflow))
                } else (currentChange / abs(prevValuation))
                
                evaluations[i].apply {
                    change = currentChange
                    percentChange = percChange
                    cumulativeChange = evaluations[i - 1].cumulativeChange!! + currentChange
                    cumulativePercentChange =
                        (1.0 + evaluations[i - 1].cumulativePercentChange!!) * (1.0 + percChange) - 1.0
                }
            } catch (e: Exception) {
                logd("performPortfolioChangeCalculation error on element ${i}, date: ${evaluations[i].date}", e)
                evaluations[i].apply {
                    valuation = valuation ?: 0.0
                    inflow = inflow ?: 0.0
                    change = 0.0
                    percentChange = 0.0
                    cumulativeChange = evaluations[i - 1].cumulativeChange
                    cumulativePercentChange = evaluations[i - 1].cumulativePercentChange
                }
            }
        }
        return true
    }
    
    override fun onDestroy() {
        logd("onDestroy")
        compositeDisposable.clear()
        super.onDestroy()
    }
}
