package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.View

interface Presenter<T : View> {
    var view: T?

    fun onDestroy(){
        view = null
    }
}