package com.example.paymentapplication.presenter

import br.com.stone.posandroid.providers.PosPrintReceiptProvider
import br.com.stone.posandroid.providers.PosTransactionProvider
import com.example.paymentapplication.infrastructure.AppStore
import com.example.paymentapplication.infrastructure.DispatcherProvider
import com.example.paymentapplication.view.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import stone.application.enums.ReceiptType
import stone.application.enums.TransactionStatusEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.CancellationProvider
import stone.providers.SendEmailTransactionProvider
import stone.repository.remote.email.pombo.email.Contact


class MainPresenterImpl(
    override var view: MainView?,
    override var dispatcherProvider: DispatcherProvider) : MainPresenter<MainView> {

    private val myScope = CoroutineScope(dispatcherProvider.main)

    override fun checkout(
        amount: Long, typeOfTransactionEnum: TypeOfTransactionEnum?,
        provider: PosTransactionProvider
    ) {
        view?.showProgress()
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                myScope.launch {
                    view?.dismissProgress()
                    if (provider.transactionStatus != TransactionStatusEnum.APPROVED) {
                        view?.showMessage(provider.transactionStatus.toString())
                    } else {
                        view?.showReceiptPrintOptions()
                    }
                }
            }

            override fun onError() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showMessage(provider.transactionStatus.toString())
                }
            }
        }
        provider.execute()
    }

    override fun refund(provider: CancellationProvider) {
        view?.showProgress()
        provider.useDefaultUI(false)
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showMessage("Transaction refund success.")
                }
            }

            override fun onError() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showMessage(provider.listOfErrors[0].toString())
                }
            }
        }
        provider.execute()
    }

    override fun printReceipt(provider: PosPrintReceiptProvider) {
        view?.showProgress()
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showMessage("Printed receipt.")
                }
            }

            override fun onError() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showMessage("Printed error.")
                }
            }
        }
        provider.execute()
    }

    override fun sendReceiptByEmail(provider: SendEmailTransactionProvider) {
        view?.showProgress()
        provider.setReceiptType(ReceiptType.CLIENT)
        provider.addTo(Contact("marcelo.pereira@stone.com.br", "Marcelo Pereira"))
        provider.from = Contact("marcelovrb@gmail.com", "Marcelo Lopes")
        provider.useDefaultUI(false)
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showMessage("Email successfully sent.")
                }
            }

            override fun onError() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showMessage(provider.listOfErrors[0].toString())
                }
            }
        }
        provider.execute()
    }
}