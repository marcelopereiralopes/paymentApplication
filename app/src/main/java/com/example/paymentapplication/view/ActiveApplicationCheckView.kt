package com.example.paymentapplication.view

interface ActiveApplicationCheckView : View {
    fun showProgress()
    fun showMessageSuccessAndNextStep(msg: String, time: Long = 1000, withProgress: Boolean = false,
                                      withNextStep: () -> Unit)
    fun showMessage(msg: String, withProgress: Boolean = false)
    fun applicationActivatedNextStep()
}