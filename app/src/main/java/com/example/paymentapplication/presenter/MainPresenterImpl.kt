package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.MainView
import stone.application.enums.TypeOfTransactionEnum
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.BluetoothConnectionProvider

class MainPresenterImpl(override var view: MainView?) : MainPresenter<MainView> {
    override fun checkout(amount: Long, installment: Int, typeOfTransactionEnum: TypeOfTransactionEnum?) {

    }

    override fun connectPINPad(bluetoothConnectionProvider: BluetoothConnectionProvider) {
        bluetoothConnectionProvider.useDefaultUI(false)
        bluetoothConnectionProvider.connectionCallback = object : StoneCallbackInterface{
            override fun onSuccess() {

            }

            override fun onError() {

            }

        }
        bluetoothConnectionProvider.execute()
    }
}