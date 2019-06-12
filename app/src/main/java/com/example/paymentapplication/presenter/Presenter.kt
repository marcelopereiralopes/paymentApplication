package com.example.paymentapplication.presenter

import com.example.paymentapplication.infrastructure.DispatcherProvider
import com.example.paymentapplication.view.View

interface Presenter<T : View> {
    var view: T?
    var dispatcherProvider: DispatcherProvider

    fun onDestroy(){
        view = null
    }
}