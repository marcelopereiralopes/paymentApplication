package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.MainView
import stone.application.enums.TypeOfTransactionEnum
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.BluetoothConnectionProvider
import stone.providers.TransactionProvider

class MainPresenterImpl(override var view: MainView?) : MainPresenter<MainView> {
    override fun checkout(
        amount: Long, typeOfTransactionEnum: TypeOfTransactionEnum?,
        transactionProvider: TransactionProvider
    ) {
        transactionProvider.useDefaultUI(false)
        view?.showProgress()
        transactionProvider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                view?.dimissProgress()
                view?.showMessage("SUCESSO")
            }

            override fun onError() {
                val error = transactionProvider.listOfErrors
                view?.dimissProgress()
                view?.showMessage("ERROR")
            }
        }

        transactionProvider.execute()
    }

    override fun connectPINPad(bluetoothConnectionProvider: BluetoothConnectionProvider) {
        view?.showProgress()
        bluetoothConnectionProvider.useDefaultUI(false)
        bluetoothConnectionProvider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                view?.dimissProgress()
                view?.showMessage("Pinpad connected.")
            }

            override fun onError() {
                view?.dimissProgress()
                view?.showMessage(bluetoothConnectionProvider.listOfErrors[0].toString())
            }

        }

        bluetoothConnectionProvider.execute()
    }
}