package com.example.paymentapplication.presenter

import com.example.paymentapplication.infrastructure.AppStore
import com.example.paymentapplication.infrastructure.DispatcherProvider
import com.example.paymentapplication.view.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import stone.application.enums.ReceiptType
import stone.application.enums.TransactionStatusEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.BluetoothConnectionProvider
import stone.providers.CancellationProvider
import stone.providers.SendEmailTransactionProvider
import stone.providers.TransactionProvider
import stone.repository.remote.email.pombo.email.Contact


class MainPresenterImpl(
    override var view: MainView?,
    override var dispatcherProvider: DispatcherProvider
) : MainPresenter<MainView> {

    private val myScope = CoroutineScope(dispatcherProvider.main)

    override fun checkout(
        amount: Long, typeOfTransactionEnum: TypeOfTransactionEnum?,
        provider: TransactionProvider
    ) {
        myScope.launch {
            provider.useDefaultUI(false)
            view?.showProgress()
            provider.connectionCallback = object : StoneCallbackInterface {
                override fun onSuccess() {
                    view?.dismissProgress()
                    if (provider.transactionStatus != TransactionStatusEnum.APPROVED) {
                        view?.showMessage(provider.transactionStatus.toString())
                    } else {
                        view?.showReceiptOptions()
                        AppStore["TRANSACTION_OBJECT"] = provider.transactionObject
                    }
                }

                override fun onError() {
                    view?.dismissProgress()
                    view?.showMessage(provider.transactionStatus.toString())
                }
            }
            withContext(dispatcherProvider.background) {
                provider.execute()
            }
        }
    }

    override fun refund(provider: CancellationProvider) {
        myScope.launch {
            view?.showProgress()
            provider.useDefaultUI(false)
            provider.connectionCallback = object : StoneCallbackInterface {
                override fun onSuccess() {
                    AppStore.remove("TRANSACTION_OBJECT")
                    view?.dismissProgress()
                    view?.showMessage("Transaction refund success.")
                }

                override fun onError() {
                    view?.dismissProgress()
                    view?.showMessage(provider.listOfErrors[0].toString())
                }
            }
            withContext(dispatcherProvider.background) {
                provider.execute()
            }
        }
    }

    override fun connectPINPad(provider: BluetoothConnectionProvider) {
        myScope.launch {
            view?.showProgress()
            provider.useDefaultUI(false)
            provider.connectionCallback = object : StoneCallbackInterface {
                override fun onSuccess() {
                    view?.dismissProgress()
                    view?.showMessage("Pinpad connected.")
                }

                override fun onError() {
                    view?.dismissProgress()
                    view?.showMessage(provider.listOfErrors[0].toString())
                }

            }
            withContext(dispatcherProvider.background) {
                provider.execute()
            }
        }
    }

    override fun sendReceiptByEmail(provider: SendEmailTransactionProvider) {
        myScope.launch {
            view?.showProgress()
            provider.setReceiptType(ReceiptType.CLIENT)
            provider.addTo(Contact("marcelo.pereira@stone.com.br", "Marcelo Pereira"))
            provider.from = Contact("marcelovrb@gmail.com", "Marcelo Lopes")
            provider.useDefaultUI(false)
            provider.connectionCallback = object : StoneCallbackInterface {
                override fun onSuccess() {
                    view?.dismissProgress()
                    view?.showMessage("Email successfully sent.")
                }

                override fun onError() {
                    view?.dismissProgress()
                    view?.showMessage(provider.listOfErrors[0].toString())
                }
            }
            withContext(dispatcherProvider.background){
                provider.execute()
            }
        }
    }
}