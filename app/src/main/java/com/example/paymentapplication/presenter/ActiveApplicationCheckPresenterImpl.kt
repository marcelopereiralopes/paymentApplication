package com.example.paymentapplication.presenter

import com.example.paymentapplication.infrastructure.DispatcherProvider
import com.example.paymentapplication.view.ActiveApplicationCheckView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.ActiveApplicationProvider

class ActiveApplicationCheckPresenterImpl(override var view: ActiveApplicationCheckView?,
                                          override var dispatcherProvider: DispatcherProvider)
    : ActiveApplicationCheckPresenter<ActiveApplicationCheckView> {

    private val myScope = CoroutineScope(dispatcherProvider.main)

    override fun activeInvoke(provider: ActiveApplicationProvider) {
        myScope.launch {
            provider.connectionCallback = object : StoneCallbackInterface {
                override fun onSuccess() {
                    view?.showMessageSuccessAndNextStep(
                        "Ativado com sucesso, \niniciando o aplicativo.",
                        withNextStep = { view?.applicationActivatedNextStep() })
                }

                override fun onError() {
                    val cause = provider.listOfErrors[0]
                    view?.showMessage("Erro na ativação do aplicativo.\n$cause")
                }
            }
            withContext(dispatcherProvider.background){
                provider.activate("846873720")
            }
        }
    }
}