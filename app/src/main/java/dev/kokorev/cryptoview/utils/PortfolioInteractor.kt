package dev.kokorev.cryptoview.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat.getString
import androidx.core.widget.addTextChangedListener
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.PortfolioCoinDB
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.databinding.AlertViewChangePositionBinding
import dev.kokorev.cryptoview.databinding.AlertViewOpenPositionBinding
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.views.fragments.toEditable
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

        val priceStr = NumbersUtils.formatPriceUSD(price)
        val symbol = coin.symbol

        // custom alert view
        val inputViewBinding = AlertViewOpenPositionBinding.inflate(layoutInflater)
        inputViewBinding.price.text = priceStr
        inputViewBinding.symbol.text = symbol
        inputViewBinding.inputValue.text = NumbersUtils.formatPriceUSD(0.0)
        inputViewBinding.input.addTextChangedListener {
            // on input calculate position value
            val num = try {
                it.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
            inputViewBinding.inputValue.text = NumbersUtils.formatPriceUSD(num * price)
        }

        MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
            .setView(inputViewBinding.root)
            .setPositiveButton(getString(context, R.string.ok)) { dialog, which ->
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
                        .setPositiveButton(getString(context, R.string.ok)) { dialogEmptyInput, whichEmptyInput ->
                            dialogEmptyInput.cancel()
                        }
                        .show()
                } else {
                    val portfolioCoinDB: PortfolioCoinDB = Converter.createPortfolioCoin(coin, price, qty)
                    repository.savePortfolioPosition(portfolioCoinDB)
                    MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
                        .setTitle(getString(context, R.string.portfolio_operation))
                        .setMessage(getString(context, R.string.position_created))
                        .setPositiveButton(getString(context, R.string.ok)) { dialogEmptyInput, whichEmptyInput ->
                            dialogEmptyInput.cancel()
                        }
                        .show()
                    Log.d(this.javaClass.simpleName, "askUserQtyToOpenPosition added to portfolio: ${portfolioCoinDB}")
                }
                Log.d(this.javaClass.simpleName, "askUserQtyToOpenPosition user input: ${qty}")
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .show()
        return
    }




    fun changePosition(portfolioCoinDB: PortfolioCoinDB) {
        repository.getCPTickerById(portfolioCoinDB.coinPaprikaId)
            .doOnSuccess { ticker ->
                val price = ticker.price ?: 0.0
                Log.d(
                    this.javaClass.simpleName,
                    "Changing position coin: ${portfolioCoinDB.symbol}, price: ${price}"
                )
                askUserQtyToChangePosition(portfolioCoinDB, price)
            }
            .doOnComplete {
                Snackbar.make(
                    view,
                    getString(context, R.string.price_not_found) + portfolioCoinDB.symbol,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .subscribe()
            .addTo(autoDisposable)
    }

    fun askUserQtyToChangePosition(
        position: PortfolioCoinDB,
        price: Double
    ) {
        if (price == 0.0) return

        val priceStr = NumbersUtils.formatPriceUSD(price)
        val symbol = position.symbol
        val oldQty = position.quantity

        // prepare custom alert view
        val inputViewBinding = AlertViewChangePositionBinding.inflate(layoutInflater)
        inputViewBinding.price.text = priceStr
        inputViewBinding.symbol.text = symbol
        inputViewBinding.inputValue.text = NumbersUtils.formatPriceUSD(oldQty * price)
        inputViewBinding.input.text = oldQty.toString().toEditable()
        inputViewBinding.input.addTextChangedListener {
            // on input calculate position value
            val num = try {
                it.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
            inputViewBinding.inputValue.text = NumbersUtils.formatPriceUSD(num * price)
        }

        MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
            .setView(inputViewBinding.root)
            .setPositiveButton(getString(context, R.string.ok)) { dialog, which ->
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
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .show()

        return
    }

    private fun changePosition(
        position: PortfolioCoinDB,
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
            ) { dialogEmptyInput, whichEmptyInput ->
                dialogEmptyInput.cancel()
            }
            .show()
        val newPrice =
            (position.priceOpen * position.quantity + price * (newQty - position.quantity)) / newQty
        position.quantity = newQty
        position.priceOpen = newPrice
        repository.savePortfolioPosition(position)
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
            ) { dialogEmptyInput, whichEmptyInput ->
                dialogEmptyInput.cancel()
            }
            .show()
    }

    private fun closePosition(
        position: PortfolioCoinDB,
        price: Double
    ) {
        val pnl = position.quantity * (price - position.priceOpen)
        val pnlStr = NumbersUtils.formatPriceUSD(pnl)
        MaterialAlertDialogBuilder(context, R.style.CVDialogStyle)
            .setTitle(getString(context, R.string.portfolio_operation))
            .setMessage(getString(context, R.string.position_closed_text) + pnlStr)
            .setPositiveButton(
                getString(
                    context,
                    R.string.ok
                )
            ) { dialogEmptyInput, whichEmptyInput ->
                dialogEmptyInput.cancel()
            }
            .show()
        repository.deletePortfolioPosition(position.id)
    }
}