package com.example.paymentapplication.view

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import br.com.stone.posandroid.providers.PosPrintReceiptProvider
import br.com.stone.posandroid.providers.PosTransactionProvider
import com.example.paymentapplication.R
import com.example.paymentapplication.infrastructure.AppStore
import com.example.paymentapplication.infrastructure.DispatcherProviderImpl
import com.example.paymentapplication.presenter.MainPresenter
import com.example.paymentapplication.presenter.MainPresenterImpl
import kotlinx.android.synthetic.main.activity_main.*
import stone.application.enums.InstalmentTransactionEnum
import stone.application.enums.ReceiptType
import stone.application.enums.TypeOfTransactionEnum
import stone.database.transaction.TransactionObject
import stone.providers.CancellationProvider
import stone.providers.SendEmailTransactionProvider
import stone.utils.Stone
import java.util.*


class MainActivity : AppCompatActivity(), MainView {

    private val mainPresenter: MainPresenter<MainView> by lazy {
        MainPresenterImpl(this, DispatcherProviderImpl())
    }

    private var transactionObject: TransactionObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkoutListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.overflow_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.refundId -> {
                AppStore["TRANSACTION_OBJECT"]?.let {
                    showAlertDialog(
                        "Do you want refund this vendor?\nValue: ${transactionObject?.amount}",
                        "Refund"
                    ) {
                        refundClickListener()
                    }
                } ?: showToastMessage("Not exist approved transaction.")
            }
            R.id.receiptEmailId -> {
                AppStore["TRANSACTION_OBJECT"]?.let {
                    showAlertDialog(
                        "Do you want send receipt by email?",
                        "Receipt"
                    ) {
                        receiptEmailClickListener()
                    }
                } ?: showToastMessage("Not exist approved transaction.")
            }
            else -> throw IllegalArgumentException()
        }

        return true
    }

    override fun showProgress() {
        hideKeyboard()
        primary.visibility = View.GONE
        secondary.visibility = View.VISIBLE
    }

    override fun dismissProgress() {
        primary.visibility = View.VISIBLE
        secondary.visibility = View.GONE
        showKeyboard()
    }

    override fun showToastMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    override fun showMessage(msg: String) {
        textView.text = msg
    }

    override fun showAlertDialog(message: String, title: String, positiveButton: () -> Unit) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(message)
            .setCancelable(true)
            .setTitle(title)
            .setPositiveButton("Yes") { _, _ ->
                positiveButton()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
            .show()
    }

    override fun showReceiptPrintOptions() {
        AppStore["TRANSACTION_OBJECT"] = transactionObject!!
        showAlertDialog("Do you want print receipt?", "Transaction Approved") {
            receiptPrintClickListener()
        }
    }

    private fun checkoutWithInstallments(amount: Long) {

        val installments =
            arrayOf("Cash Payment", "2 - Installments", "3 - Installments", "4 - Installments")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Number of Installments:")

        builder.setItems(installments) { _, which ->

            when (which) {
                0 -> {
                    checkout(
                        TypeOfTransactionEnum.CREDIT,
                        InstalmentTransactionEnum.ONE_INSTALMENT,
                        amount
                    )
                }
                1 -> {
                    checkout(
                        TypeOfTransactionEnum.CREDIT,
                        InstalmentTransactionEnum.TWO_INSTALMENT_NO_INTEREST,
                        amount
                    )
                }
                2 -> {
                    checkout(
                        TypeOfTransactionEnum.CREDIT,
                        InstalmentTransactionEnum.THREE_INSTALMENT_NO_INTEREST,
                        amount
                    )
                }
                3 -> {
                    checkout(
                        TypeOfTransactionEnum.CREDIT,
                        InstalmentTransactionEnum.FOUR_INSTALMENT_NO_INTEREST,
                        amount
                    )
                }
            }
        }

        builder.create().show()
    }

    private fun hideKeyboard() {
        val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(primary.windowToken, 0)
    }

    private fun showKeyboard() {
        val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(value, 0)
    }

    private fun receiptPrintClickListener() {
        val posPrintReceiptProvider = PosPrintReceiptProvider(
            this,
            AppStore["TRANSACTION_OBJECT"] as TransactionObject,
            ReceiptType.MERCHANT
        )
        mainPresenter.printReceipt(posPrintReceiptProvider)
    }

    private fun receiptEmailClickListener() {
        val emailTransactionProvider = SendEmailTransactionProvider(
            this,
            AppStore["TRANSACTION_OBJECT"] as TransactionObject
        )
        mainPresenter.sendReceiptByEmail(emailTransactionProvider)
    }

    private fun refundClickListener() {
        val cancellationProvider = CancellationProvider(
            this,
            AppStore["TRANSACTION_OBJECT"] as TransactionObject
        )
        mainPresenter.refund(cancellationProvider)
    }

    private fun checkoutListener() {
        checkout.setOnClickListener {
            val cleanString = clearCurrencyFormatter(value.text.toString())
            val amount = if (cleanString == "") 0 else cleanString.toLong()

            if (amount > 0) {
                val typeOfTransactionEnum = when (radioGroup.checkedRadioButtonId) {
                    R.id.radioButtonCredit -> TypeOfTransactionEnum.CREDIT
                    R.id.radioButtonDebit -> TypeOfTransactionEnum.DEBIT
                    R.id.radioButtonVoucher -> TypeOfTransactionEnum.VOUCHER
                    else -> throw IllegalArgumentException()
                }

                if (typeOfTransactionEnum == TypeOfTransactionEnum.CREDIT)
                    checkoutWithInstallments(amount)
                else
                    checkout(
                        typeOfTransactionEnum,
                        InstalmentTransactionEnum.ONE_INSTALMENT,
                        amount
                    )

            } else {
                showToastMessage("Invalid input value.")
            }
        }
    }

    private fun checkout(
        typeOfTransactionEnum: TypeOfTransactionEnum,
        instalmentTransactionEnum: InstalmentTransactionEnum,
        amount: Long
    ) {
        transactionObject =
            createTransactionObject(typeOfTransactionEnum, instalmentTransactionEnum, amount)

        val provider =
            PosTransactionProvider(this, transactionObject, Stone.getUserModel(0))

        mainPresenter.checkout(
            amount = amount, typeOfTransactionEnum = typeOfTransactionEnum,
            provider = provider
        )
    }

    private fun createTransactionObject(
        typeOfTransactionEnum: TypeOfTransactionEnum,
        instalmentTransactionEnum: InstalmentTransactionEnum,
        amount: Long
    ): TransactionObject {
        val transactionObject = TransactionObject()
        transactionObject.amount = amount.toString()
        transactionObject.initiatorTransactionKey = UUID.randomUUID().toString()
        transactionObject.instalmentTransaction = instalmentTransactionEnum
        transactionObject.typeOfTransaction = typeOfTransactionEnum
        transactionObject.isCapture = true
        transactionObject.subMerchantCity = "Rio"
        transactionObject.subMerchantAddress = "00000000"
        transactionObject.subMerchantRegisteredIdentifier = "00000000"
        transactionObject.subMerchantTaxIdentificationNumber = "33368443000199"

        return transactionObject
    }

    private fun clearCurrencyFormatter(value: String) = value.replace("[,.]".toRegex(), "")

}