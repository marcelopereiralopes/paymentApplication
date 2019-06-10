package com.example.paymentapplication.view

import android.bluetooth.BluetoothAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.paymentapplication.R
import com.example.paymentapplication.presenter.MainPresenter
import com.example.paymentapplication.presenter.MainPresenterImpl
import kotlinx.android.synthetic.main.activity_main.*
import stone.application.enums.TypeOfTransactionEnum
import stone.providers.BluetoothConnectionProvider
import stone.utils.PinpadObject
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity(), MainView {

    private val mainPresenter: MainPresenter<MainView> by lazy {
        MainPresenterImpl(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkout.setOnClickListener {
            val amount = if (amount.text.toString() == "") 0 else amount.text.toString().toLong()

            if (amount > 0) {
                val typeOfTransactionEnum = when (radioGroup.checkedRadioButtonId) {
                    R.id.radioButtonCredit -> TypeOfTransactionEnum.CREDIT
                    R.id.radioButtonDebit -> TypeOfTransactionEnum.DEBIT
                    R.id.radioButtonVoucher -> TypeOfTransactionEnum.VOUCHER
                    else -> throw IllegalArgumentException()
                }

                mainPresenter.checkout(amount = amount, typeOfTransactionEnum = typeOfTransactionEnum)
            } else {
                Toast.makeText(this, "Invalid input value.", Toast.LENGTH_SHORT).show()
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
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val bondedDevices = bluetoothAdapter.bondedDevices
            if (bondedDevices.isNotEmpty()) {
                val pinPadObject = PinpadObject("PAX-6A802929",
                    bondedDevices.elementAt(0).address, true)
                mainPresenter.connectPINPad(BluetoothConnectionProvider(this, pinPadObject))
            } else {
                Toast.makeText(this, "Please, first pair your PINPad with your Android phone.", Toast.LENGTH_SHORT).show()
            }
        } else {
            throw IllegalArgumentException()
        }

        return true
    }
}