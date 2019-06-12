package com.example.paymentapplication.view

import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentapplication.R
import com.example.paymentapplication.infrastructure.AppStore
import com.example.paymentapplication.infrastructure.DispatcherProviderImpl
import com.example.paymentapplication.presenter.MainPresenter
import com.example.paymentapplication.presenter.MainPresenterImpl
import kotlinx.android.synthetic.main.activity_main.*
import stone.application.enums.InstalmentTransactionEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.database.transaction.TransactionObject
import stone.providers.BluetoothConnectionProvider
import stone.providers.CancellationProvider
import stone.providers.SendEmailTransactionProvider
import stone.providers.TransactionProvider
import stone.user.UserModel
import stone.utils.PinpadObject
import stone.utils.Stone
import java.util.*

class MainActivity : AppCompatActivity(), MainView {

    private val mainPresenter: MainPresenter<MainView> by lazy {
        MainPresenterImpl(this, DispatcherProviderImpl())
    }

    private var pinpadObject: PinpadObject? = null
    private var transactionObject: TransactionObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Stone.getPinpadObjectList().isNotEmpty()) pinpadObject = Stone.getPinpadObjectList()[0]

        checkoutListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.overflow_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when {
            item.itemId == R.id.connectId -> {
                if (pinpadObject == null) {
                    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val bondedDevices = bluetoothAdapter?.bondedDevices

                    if (bondedDevices != null && bondedDevices.isNotEmpty()) {
                        pinpadObject = PinpadObject("PAX-6A802929",
                            bondedDevices.elementAt(0).address, true)
                    } else {
                        showMessage("Pair your PINPad with mobile phone")
                        return true
                    }
                }
                mainPresenter.connectPINPad(BluetoothConnectionProvider(this, pinpadObject))
            }

            item.itemId == R.id.refundId -> {
                AppStore["TRANSACTION_OBJECT"]?.let {
                    showAlertDialog(
                        "Do you want refund this vendor?\nValue: ${transactionObject?.amount}",
                        "Refund"
                    ) {
                        refundClickListener()
                    }
                } ?: showMessage("Not exist approved transaction.")
            }

            else -> throw IllegalArgumentException()
        }

        return true
    }

    override fun showProgress() {
        checkout.isEnabled = false
        radioGroup.isEnabled = false
        value.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }

    override fun dismissProgress() {
        checkout.isEnabled = true
        radioGroup.isEnabled = true
        value.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun showAlertDialog(message: String, title: String, positiveButton: () -> Unit) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(message)
            .setCancelable(true)
            .setTitle(title)
            .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ -> positiveButton() })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, _ -> dialogInterface.cancel() })
            .create()
            .show()
    }

    override fun showReceiptOptions() {
        showAlertDialog("Do you want send receipt?", "Receipt") {
            receiptEmailClickListener()
        }
    }

    private fun receiptEmailClickListener() {
        val sendEmailTransactionProvider = SendEmailTransactionProvider(
            this,
            AppStore["TRANSACTION_OBJECT"] as TransactionObject
        )
        mainPresenter.sendReceiptByEmail(sendEmailTransactionProvider)
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
            val amount = if (value.text.toString() == "") 0 else value.text.toString().toLong()

            if (amount > 0) {
                pinpadObject?.let {
                    val typeOfTransactionEnum = when (radioGroup.checkedRadioButtonId) {
                        R.id.radioButtonCredit -> TypeOfTransactionEnum.CREDIT
                        R.id.radioButtonDebit -> TypeOfTransactionEnum.DEBIT
                        R.id.radioButtonVoucher -> TypeOfTransactionEnum.VOUCHER
                        else -> throw IllegalArgumentException()
                    }

                    transactionObject = createTransactionObject(typeOfTransactionEnum, amount)

                    val provider = TransactionProvider(
                        this, createTransactionObject(typeOfTransactionEnum, amount),
                        (AppStore["USER_LIST"] as List<UserModel>?)?.get(0), it
                    )

                    mainPresenter.checkout(
                        amount = amount, typeOfTransactionEnum = typeOfTransactionEnum,
                        provider = provider
                    )
                } ?: showMessage("Connect your PINPad with mobile phone")
            } else {
                showMessage("Invalid input value.")
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
}