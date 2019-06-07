package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.View
import stone.providers.ActiveApplicationProvider

interface ActiveApplicationCheckPresenter<T : View> : Presenter<T> {
    fun activeInvoke(activeApplicationProvider: ActiveApplicationProvider)
}