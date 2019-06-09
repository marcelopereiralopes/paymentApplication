package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.ActiveApplicationCheckView
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.ActiveApplicationProvider

class ActiveApplicationCheckPresenterImpl(
    override var view: ActiveApplicationCheckView?
) : ActiveApplicationCheckPresenter<ActiveApplicationCheckView> {

    override fun activeInvoke(activeApplicationProvider: ActiveApplicationProvider) {
        activeApplicationProvider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                view?.showMessageSuccessAndNextStep(
                    "Ativado com sucesso, \niniciando o aplicativo.",
                    withNextStep = { view?.applicationActivatedNextStep() })
            }

            override fun onError() {
                val cause = activeApplicationProvider.listOfErrors[0]
                view?.showMessage("Erro na ativação do aplicativo.\n$cause")
            }
        }

        activeApplicationProvider.activate("846873720")
    }
}