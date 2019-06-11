package com.example.paymentapplication.view


interface MainView : View {
    fun showProgress()
    fun dismissProgress()
    fun showMessage(msg: String)
    fun showDialogSendEmail()
}