package com.example.paymentapplication.view


interface MainView : View {
    fun showProgress()
    fun dismissProgress()
    fun showReceiptPrintOptions()
    fun showToastMessage(msg: String)
    fun showMessage(msg: String)
    fun showAlertDialog(message: String, title: String, positiveButton: () -> Unit)
}