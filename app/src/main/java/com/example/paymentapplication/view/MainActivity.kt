package com.example.paymentapplication.view

import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
import java.text.NumberFormat
import java.util.*

class MainActivity : AppCompatActivity(), MainView {

    private val mainPresenter: MainPresenter<MainView> by lazy {
        MainPresenterImpl(this, DispatcherProviderImpl())
    }

    private var transactionObject: TransactionObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applyMoneyMask(value)
        checkoutListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.overflow_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when {
            item.itemId == R.id.refundId -> {
                AppStore["TRANSACTION_OBJECT"]?.let {
                    showAlertDialog(
                        "Do you want refund this vendor?\nValue: ${transactionObject?.amount}",
                        "Refund"
                    ) {
                        refundClickListener()
                    }
                } ?: showToastMessage("Not exist approved transaction.")
            }

            item.itemId == R.id.receiptEmailId -> {
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
            .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ -> positiveButton() })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ ->
                dialogInterface.cancel() })
            .create()
            .show()
    }

    override fun showReceiptPrintOptions() {
        AppStore["TRANSACTION_OBJECT"] = transactionObject!!
        showAlertDialog("Do you want print receipt?", "Transaction Approved") {
            receiptPrintClickListener()
        }
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

                transactionObject = createTransactionObject(typeOfTransactionEnum, amount)

                val provider = PosTransactionProvider(this, transactionObject, Stone.getUserModel(0))

                mainPresenter.checkout(
                    amount = amount, typeOfTransactionEnum = typeOfTransactionEnum,
                    provider = provider
                )
            } else {
                showToastMessage("Invalid input value.")
            }
        }
    }

    private fun createTransactionObject(typeOfTransactionEnum: TypeOfTransactionEnum, amount: Long): TransactionObject {
        val transactionObject = TransactionObject()
        transactionObject.amount = amount.toString()
        transactionObject.initiatorTransactionKey = UUID.randomUUID().toString()
        transactionObject.instalmentTransaction = InstalmentTransactionEnum.ONE_INSTALMENT
        transactionObject.typeOfTransaction = typeOfTransactionEnum
        transactionObject.isCapture = true
        transactionObject.subMerchantCity = "Rio"
        transactionObject.subMerchantAddress = "00000000"
        transactionObject.subMerchantRegisteredIdentifier = "00000000"
        transactionObject.subMerchantTaxIdentificationNumber = "33368443000199"

        return transactionObject
    }

    private fun applyMoneyMask(inputValue: EditText?) {
        var current = ""

        inputValue?.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() != current) {
                    inputValue.removeTextChangedListener(this)

                    val cleanString = clearCurrencyFormatter(p0.toString())

                    val parsed = cleanString.toDouble()
                    val formatted = NumberFormat
                        .getCurrencyInstance(Locale("pt", "BR")).format((parsed / 100))

                    current = formatted
                    inputValue.setText(formatted)
                    inputValue.setSelection(formatted.length)

                    inputValue.addTextChangedListener(this)
                }
            }

        })
    }

    private fun clearCurrencyFormatter(value: String) = value.replace("[R$,.]".toRegex(), "")
}