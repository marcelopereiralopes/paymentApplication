package com.example.paymentapplication.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.paymentapplication.R
import com.example.paymentapplication.presenter.MainPresenter
import com.example.paymentapplication.presenter.MainPresenterImpl
import kotlinx.android.synthetic.main.activity_main.*
import stone.application.enums.TypeOfTransactionEnum
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
                val typeOfTransactionEnum = when(radioGroup.checkedRadioButtonId){
                    R.id.radioButtonCredit -> TypeOfTransactionEnum.CREDIT
                    R.id.radioButtonDebit -> TypeOfTransactionEnum.DEBIT
                    R.id.radioButtonVoucher -> TypeOfTransactionEnum.VOUCHER
                    else -> throw IllegalArgumentException()
                }

                mainPresenter.checkout(amount = amount, typeOfTransactionEnum = typeOfTransactionEnum)
            } else {
                Toast.makeText(this, "Invalid input parameters.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}