package com.example.paymentapplication.view

interface ActiveApplicationCheckView : View {
    fun showProgress()
    fun showMessage(msg: String, time: Long = 1000, withoutProgress: Boolean = false)
}