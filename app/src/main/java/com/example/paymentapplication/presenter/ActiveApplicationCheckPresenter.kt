package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.View
import stone.providers.ActiveApplicationProvider
import stone.user.UserModel

interface ActiveApplicationCheckPresenter<T : View> : Presenter<T> {
    fun handleListData(list: List<UserModel>)
    fun handleEmptyListData(activeApplicationProvider: ActiveApplicationProvider)
}