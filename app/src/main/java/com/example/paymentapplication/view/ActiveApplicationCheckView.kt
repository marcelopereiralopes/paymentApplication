package com.example.paymentapplication.view

interface ActiveApplicationCheckView : View {
    fun showProgress()
    fun dismissProgress()
    fun applicationActivatedNextStep()
}