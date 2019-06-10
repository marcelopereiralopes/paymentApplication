package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.View
import stone.application.enums.TypeOfTransactionEnum


interface MainPresenter<T: View> : Presenter<T> {
    fun checkout(amount: Long, installment: Int = 1, typeOfTransactionEnum: TypeOfTransactionEnum?)
}