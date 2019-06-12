package com.example.paymentapplication.presenter

import br.com.stone.posandroid.providers.PosPrintReceiptProvider
import br.com.stone.posandroid.providers.PosTransactionProvider
import com.example.paymentapplication.view.View
import stone.application.enums.TypeOfTransactionEnum
import stone.providers.BluetoothConnectionProvider
import stone.providers.CancellationProvider
import stone.providers.SendEmailTransactionProvider
import stone.providers.TransactionProvider


interface MainPresenter<T: View> : Presenter<T> {
    fun checkout(amount: Long, typeOfTransactionEnum: TypeOfTransactionEnum?, provider: PosTransactionProvider)
    fun refund(provider: CancellationProvider)
    fun sendReceiptByEmail(provider: SendEmailTransactionProvider)
    fun printReceipt(provider: PosPrintReceiptProvider)
}