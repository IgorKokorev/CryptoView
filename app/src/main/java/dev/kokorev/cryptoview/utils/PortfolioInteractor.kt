package dev.kokorev.cryptoview.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat.getString
import androidx.core.widget.addTextChangedListener
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import com.coinpaprika.apiclient.entity.PortfolioTransactionDB
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.AlertViewChangePositionBinding
import dev.kokorev.cryptoview.databinding.AlertViewOpenPositionBinding
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.views.fragments.toEditable
import java.time.Instant
import javax.inject.Inject

class PortfolioInteractor(val view: View, val autoDisposable: AutoDisposable) {
    @Inject
    lateinit var repository: Repository

    private val context: Context = view.context
    val layoutInflater: LayoutInflater = context.getSystemService(LayoutInflater::class.java)

    init {
        App.instance.dagger.inject(this)
    }

    // open new position in portfolio on the coin
    fun openPosition(coin: CoinDetailsEntity) {
        // looking for coin price
        repository.getCPTickerById(coin.id)
            .doOnSuccess { ticker ->
                val price = ticker.price ?: 0.0
                askUserQtyToOpenPosition(coin, price)
            }
            .doOnComplete {
                Snackbar.make(
                    view,
                    getString(context, R.string.price_not_found) + coin.symbol,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .subscribe()
            .addTo(autoDisposable)
    }

    // get the quantity of coins to open position from user input
    fun askUserQtyToOpenPosition(
        coin: CoinDetailsEntity,
        price: Double
    ) {
        if (price == 0.0) return

        val priceStr = NumbersUtils.formatPriceWithCurrency(price)
        val symbol = coin.symbol

        // custom alert view
        val inputViewBinding = AlertViewOpenPositionBinding.inflate(layoutInflater)
        inputViewBinding.price.text = priceStr
        inputViewBinding.symbol.text = symbol
        inputViewBinding.inputValue.text = NumbersUtils.formatPriceWithCurrency(0.0)
        inputViewBinding.input.addTextChangedListener {
            // on input calculate position value
            val num = try {
                it.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
            inputViewBinding.inputValue.text = NumbersUtils.formatPriceWithCurrency(num * price)
        }

        MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
            .setView(inputViewBinding.root)
            .setPositiveButton(getString(context, R.string.ok)) { dialog, _ ->
                val qty = try {
                    inputViewBinding.input.text.toString().toDouble()
                } catch (e: NumberFormatException) {
                    0.0
                }
                // If 0 is entered - no position created
                if (qty == 0.0) {
                    MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
                        .setTitle(getString(context, R.string.portfolio_operation))
                        .setMessage(getString(context, R.string.no_quantity_entered_the_position_isn_t_saved))
                        .setPositiveButton(getString(context, R.string.ok)) { dialogEmptyInput, _ ->
                            dialogEmptyInput.cancel()
                        }
                        .show()
                } else {
                    val portfolioPositionDB: PortfolioPositionDB = Converter.createPortfolioPosition(coin, price, qty)
                    
                    performTransaction(portfolioPositionDB, price, qty)
                    
                    MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
                        .setTitle(getString(context, R.string.portfolio_operation))
                        .setMessage(getString(context, R.string.position_created))
                        .setPositiveButton(getString(context, R.string.ok)) { dialogEmptyInput, _ ->
                            dialogEmptyInput.cancel()
                        }
                        .show()
                    Log.d(this.javaClass.simpleName, "askUserQtyToOpenPosition added to portfolio: ${portfolioPositionDB}")
                }
                Log.d(this.javaClass.simpleName, "askUserQtyToOpenPosition user input: ${qty}")
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
        return
    }

    
    fun changePosition(portfolioPositionDB: PortfolioPositionDB) {
        repository.getCPTickerById(portfolioPositionDB.coinPaprikaId)
            .doOnSuccess { ticker ->
                val price = ticker.price ?: 0.0
                Log.d(
                    this.javaClass.simpleName,
                    "Changing position coin: ${portfolioPositionDB.symbol}, price: ${price}"
                )
                askUserQtyToChangePosition(portfolioPositionDB, price)
            }
            .doOnComplete {
                Snackbar.make(
                    view,
                    getString(context, R.string.price_not_found) + portfolioPositionDB.symbol,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .subscribe()
            .addTo(autoDisposable)
    }

    fun askUserQtyToChangePosition(
        position: PortfolioPositionDB,
        price: Double
    ) {
        if (price == 0.0) return

        val priceStr = NumbersUtils.formatPriceWithCurrency(price)
        val symbol = position.symbol
        val oldQty = position.quantity

        // prepare custom alert view
        val inputViewBinding = AlertViewChangePositionBinding.inflate(layoutInflater)
        inputViewBinding.price.text = priceStr
        inputViewBinding.symbol.text = symbol
        inputViewBinding.inputValue.text = NumbersUtils.formatPriceWithCurrency(oldQty * price)
        inputViewBinding.input.text = oldQty.toString().toEditable()
        inputViewBinding.input.addTextChangedListener {
            // on input calculate position value
            val num = try {
                it.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
            inputViewBinding.inputValue.text = NumbersUtils.formatPriceWithCurrency(num * price)
        }

        MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
            .setView(inputViewBinding.root)
            .setPositiveButton(getString(context, R.string.ok)) { dialog, _ ->
                val newQty = try {
                    inputViewBinding.input.text.toString().toDouble()
                } catch (e: NumberFormatException) {
                    oldQty
                }
                // If 0 is entered - the position is to close
                if (newQty == 0.0) {
                    closePosition(position, price)
                } else if (newQty == oldQty) {
                    showNoChanges()
                } else {
                    changePosition(position, newQty, price)
                }
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
        return
    }

    private fun changePosition(
        position: PortfolioPositionDB,
        newQty: Double,
        price: Double
    ) {
        MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
            .setTitle(getString(context, R.string.portfolio_operation))
            .setMessage(getString(context, R.string.position_changed_text) + newQty)
            .setPositiveButton(
                getString(
                    context,
                    R.string.ok
                )
            ) { dialogEmptyInput, _ ->
                dialogEmptyInput.cancel()
            }
            .show()
        val dealValue = price * (newQty - position.quantity)
        val newPrice =
            (position.priceOpen * position.quantity + dealValue) / newQty
        val oldQty = position.quantity
        position.quantity = newQty
        position.priceOpen = newPrice
        performTransaction(position, price = price, qty = newQty - oldQty)
    }

    private fun showNoChanges() {
        MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
            .setTitle(getString(context, R.string.portfolio_operation))
            .setMessage(getString(context, R.string.position_not_changed))
            .setPositiveButton(
                getString(
                    context,
                    R.string.ok
                )
            ) { dialogEmptyInput, _ ->
                dialogEmptyInput.cancel()
            }
            .show()
    }

    private fun closePosition(
        position: PortfolioPositionDB,
        price: Double
    ) {
        val oldQty = position.quantity
        val pnl = oldQty * (price - position.priceOpen)
        val pnlStr = NumbersUtils.formatPriceWithCurrency(pnl)
        MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
            .setTitle(getString(context, R.string.portfolio_operation))
            .setMessage(getString(context, R.string.position_closed_text) + pnlStr)
            .setPositiveButton(
                getString(
                    context,
                    R.string.ok
                )
            ) { dialogEmptyInput, _ ->
                dialogEmptyInput.cancel()
            }
            .show()
        position.quantity = 0.0
        performTransaction(position, price = price, qty = -oldQty)
    }
    
    
    private fun performTransaction(position: PortfolioPositionDB, price: Double, qty: Double) {
        // saving portfolio state
        if (position.quantity == 0.0) {
            repository.deletePortfolioPosition(position.id)
        } else {
            repository.savePortfolioPosition(position)
        }
        // saving inflow/outflow for today's portfolio valuation
        saveInflowToPortfolioEvaluation(price * qty)
        // saving the transaction to db
        saveTransaction(position.coinPaprikaId, price, qty)
    }
    
    private fun saveTransaction(coinPaprikaId: String, price: Double, qty: Double) {
        val transaction = PortfolioTransactionDB(
            coinPaprikaId = coinPaprikaId,
            time = Instant.now(),
            price = price,
            quantity = qty,
        )
        repository.savePortfolioTransaction(transaction)
    }
    
    private fun saveInflowToPortfolioEvaluation(dealValue: Double) {
        val today = Instant.now().toLocalDate()

        repository.getPortfolioEvaluationByDate(today)
            .doOnSuccess {  evaluation ->
                Log.d(this.javaClass.simpleName, "Portfolio evaluation found for today. Valuation: ${evaluation.valuation}, Inflow: ${evaluation.inflow}")
                evaluation.inflow = (evaluation.inflow ?: 0.0) + dealValue
                repository.savePortfolioEvaluation(evaluation)
            }
            .doOnComplete {
                Log.d(this.javaClass.simpleName, "No portfolio evaluation found for today")
                val portfolioEvaluation = PortfolioEvaluationDB(
                    date = today,
                    valuation = null,
                    inflow = dealValue,
                    change = null,
                    percentChange = null,
                    positions = null,
                )
                repository.savePortfolioEvaluation(portfolioEvaluation)
            }
            .doOnError {
                Log.d(this.javaClass.simpleName, "Error getting portfolio evaluation, message: ${it.localizedMessage}, ${it.stackTrace}")
            }
            .subscribe()
            .addTo(autoDisposable)
    }
}