package com.example.paymentapplication.view

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentapplication.R
import com.example.paymentapplication.infrastructure.AppStore
import com.example.paymentapplication.presenter.MainPresenter
import com.example.paymentapplication.presenter.MainPresenterImpl
import kotlinx.android.synthetic.main.activity_main.*
import stone.application.enums.InstalmentTransactionEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.database.transaction.TransactionObject
import stone.providers.BluetoothConnectionProvider
import stone.providers.TransactionProvider
import stone.user.UserModel
import stone.utils.PinpadObject
import java.util.*

class MainActivity : AppCompatActivity(), MainView {

    private val mainPresenter: MainPresenter<MainView> by lazy {
        MainPresenterImpl(this)
    }

    private var pinpadObject: PinpadObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bondedDevices = bluetoothAdapter.bondedDevices
        if (bondedDevices.isNotEmpty()) {
            pinpadObject = PinpadObject(
                "PAX-6A802929",
                bondedDevices.elementAt(0).address, true
            )
        }

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

                    val provider = TransactionProvider(
                        this, createTransactionObject(typeOfTransactionEnum, amount),
                        (AppStore.instance["USER_LIST"] as List<UserModel>?)?.get(0), it)

                    mainPresenter.checkout(
                        amount = amount, typeOfTransactionEnum = typeOfTransactionEnum,
                        transactionProvider = provider
                    )
                } ?: showMessage("Pair your PINPad with your mobile phone")
            } else {
                showMessage("Invalid input value.")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.overflow_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.connectId) {
            mainPresenter.connectPINPad(BluetoothConnectionProvider(this, pinpadObject))
        } else {
            throw IllegalArgumentException()
        }

        return true
    }

    override fun showProgress() {
        checkout.isEnabled = false
        radioGroup.isEnabled = false
        value.isEnabled = false
        progressBar.visibility = View.VISIBLE
    }

    override fun dimissProgress() {
        checkout.isEnabled = true
        radioGroup.isEnabled = true
        value.isEnabled = true
        progressBar.visibility = View.GONE
    }

    override fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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