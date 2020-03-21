package com.example.paymentapplication.presenter

import com.example.paymentapplication.infrastructure.DispatcherProvider
import com.example.paymentapplication.view.ActiveApplicationCheckView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import stone.application.interfaces.StoneCallbackInterface
import stone.providers.ActiveApplicationProvider

class ActiveApplicationCheckPresenterImpl(
    override var view: ActiveApplicationCheckView?,
    override var dispatcherProvider: DispatcherProvider
) : ActiveApplicationCheckPresenter<ActiveApplicationCheckView> {

    private val myScope = CoroutineScope(dispatcherProvider.main)

    override fun activeInvoke(stoneCode: String, provider: ActiveApplicationProvider) {

        view?.showProgress()

        provider.connectionCallback = object : StoneCallbackInterface {
            override fun onSuccess() {
                myScope.launch {
                    view?.applicationActivatedNextStep()
                }
            }

            override fun onError() {
                val cause = provider.listOfErrors[0]
                myScope.launch {
                    view?.dismissProgress()
                }
            }
        }

        provider.activate(stoneCode)
    }
}