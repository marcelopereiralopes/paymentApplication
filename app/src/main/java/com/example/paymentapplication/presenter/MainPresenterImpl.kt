package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.MainView
import stone.application.enums.TypeOfTransactionEnum


class MainPresenterImpl(override var view: MainView?) : MainPresenter<MainView> {

    override fun checkout(amount: Long, installment: Int, typeOfTransactionEnum: TypeOfTransactionEnum?) {

    }

}