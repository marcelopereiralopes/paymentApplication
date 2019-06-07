package com.example.paymentapplication.presenter

import com.example.paymentapplication.view.ActiveApplicationCheckView
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.ActiveApplicationProvider
import stone.user.UserModel

class ActiveApplicationCheckPresenterImpl(
    override var view: ActiveApplicationCheckView?
) : ActiveApplicationCheckPresenter<ActiveApplicationCheckView> {

    override fun handleListData(list: List<UserModel>) {
        view?.showMessage("ativado anteriormente.")
    }

    override fun handleEmptyListData(activeApplicationProvider: ActiveApplicationProvider) {
        activeApplicationProvider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                view?.showMessage("Ativado com sucesso, iniciando o aplicativo.",
                    withoutProgress = true)
            }

            override fun onError() {
                view?.showMessage("Erro na ativação do aplicativo, verifique a lista de erros do provide.",
                    withoutProgress = true)
                //view?.dismissProgress()
                activeApplicationProvider.listOfErrors
            }
        }

        activeApplicationProvider.activate("846873720")
    }
}