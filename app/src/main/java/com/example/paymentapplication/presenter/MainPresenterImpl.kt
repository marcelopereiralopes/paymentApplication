package com.example.paymentapplication.presenter

import br.com.stone.posandroid.providers.PosPrintReceiptProvider
import br.com.stone.posandroid.providers.PosTransactionProvider
import com.example.paymentapplication.infrastructure.DispatcherProvider
import com.example.paymentapplication.view.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import stone.application.enums.Action
import stone.application.enums.ReceiptType
import stone.application.enums.TransactionStatusEnum
import stone.application.enums.TypeOfTransactionEnum
import stone.application.interfaces.StoneActionCallback
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.CancellationProvider
import stone.providers.SendEmailTransactionProvider
import stone.repository.remote.email.pombo.email.Contact


class MainPresenterImpl(
    override var view: MainView?,
    override var dispatcherProvider: DispatcherProvider
) : MainPresenter<MainView> {

    private val myScope = CoroutineScope(dispatcherProvider.main)

    override fun checkout(
        amount: Long, typeOfTransactionEnum: TypeOfTransactionEnum?,
        provider: PosTransactionProvider
    ) {
        view?.showProgress()
        provider.connectionCallback = object : StoneActionCallback {
            override fun onSuccess() {
                myScope.launch {
                    if (provider.transactionStatus != TransactionStatusEnum.APPROVED)
                        view?.showToastMessage(provider.transactionStatus.toString())
                }
            }

            override fun onError() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showToastMessage(provider.transactionStatus.toString())
                }
            }

            override fun onStatusChanged(p0: Action?) {
                myScope.launch {
                    view?.showMessage(translateStatusMessage(p0.toString()))
                    if (p0.toString() == "TRANSACTION_CARD_REMOVED"
                        && provider.transactionStatus == TransactionStatusEnum.APPROVED){
                        view?.dismissProgress()
                        view?.showReceiptPrintOptions()
                    }
                }
            }
        }
        provider.execute()
    }

    override fun refund(provider: CancellationProvider) {
        view?.showProgress()
        view?.showMessage("refunding")
        provider.useDefaultUI(false)
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showToastMessage("Transaction refund success.")
                }
            }

            override fun onError() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showToastMessage(provider.listOfErrors[0].toString())
                }
            }
        }
        provider.execute()
    }

    override fun printReceipt(provider: PosPrintReceiptProvider) {
        view?.showProgress()
        view?.showMessage("printing")
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showToastMessage("Printed receipt.")
                }
            }

            override fun onError() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showToastMessage("Printed error.")
                }
            }
        }
        provider.execute()
    }

    override fun sendReceiptByEmail(provider: SendEmailTransactionProvider) {
        view?.showProgress()
        view?.showMessage("sending receipt")
        provider.setReceiptType(ReceiptType.CLIENT)
        provider.addTo(Contact("marcelo.pereira@stone.com.br", "Marcelo Pereira"))
        provider.from = Contact("marcelovrb@gmail.com", "Marcelo Lopes")
        provider.useDefaultUI(false)
        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showToastMessage("Email successfully sent.")
                }
            }

            override fun onError() {
                myScope.launch {
                    view?.dismissProgress()
                    view?.showToastMessage(provider.listOfErrors[0].toString())
                }
            }
        }
        provider.execute()
    }

    fun translateStatusMessage(message: String): String {
        return when(message){
            "TRANSACTION_WAITING_CARD" -> "Insert or pass the card"
            "TRANSACTION_WAITING_PASSWORD" -> "Insert password"
            "TRANSACTION_SENDING" -> "Sending transaction"
            "TRANSACTION_REMOVE_CARD" -> "Remove the card"
            "TRANSACTION_CARD_REMOVED" -> "Card removed"
            else -> ""
        }
    }
}