package com.example.paymentapplication.view


interface MainView : View {
    fun showProgress()
    fun dimissProgress()
    fun showMessage(msg: String)
}