package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.View
import stone.application.enums.TypeOfTransactionEnum
import stone.providers.BluetoothConnectionProvider
import stone.providers.CancellationProvider
import stone.providers.SendEmailTransactionProvider
import stone.providers.TransactionProvider


interface MainPresenter<T: View> : Presenter<T> {
    fun checkout(amount: Long, typeOfTransactionEnum: TypeOfTransactionEnum?, provider: TransactionProvider)
    fun refund(provider: CancellationProvider)
    fun connectPINPad(provider: BluetoothConnectionProvider)
    fun sendReceiptByEmail(provider: SendEmailTransactionProvider)
}