package com.example.paymentapplication.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentapplication.R
import com.example.paymentapplication.infrastructure.AppStore
import com.example.paymentapplication.presenter.ActiveApplicationCheckPresenter
import com.example.paymentapplication.presenter.ActiveApplicationCheckPresenterImpl
import kotlinx.android.synthetic.main.activity_active_application_check.*
import stone.application.StoneStart
import stone.environment.Environment.SANDBOX
import stone.providers.ActiveApplicationProvider
import stone.utils.Stone

class ActiveApplicationCheckActivity : AppCompatActivity(), ActiveApplicationCheckView {

    private val activeApplicationCheckPresenter:
            ActiveApplicationCheckPresenter<ActiveApplicationCheckView> by lazy {
        ActiveApplicationCheckPresenterImpl(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_application_check)

        Stone.setEnvironment(SANDBOX)
        Stone.setAppName(getString(R.string.app_name))
        StoneStart.init(this)

        AppStore["USER_LIST"]?.let {
            applicationActivatedNextStep()
        }?: activeApplicationCheckPresenter.activeInvoke(ActiveApplicationProvider(this))
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    override fun showMessageSuccessAndNextStep(
        msg: String,
        time: Long,
        withProgress: Boolean,
        withNextStep: () -> Unit
    ) {
        AppStore["USER_LIST"] = StoneStart.init(this)
        showMessage(msg, withProgress)
        Handler().postDelayed({
            withNextStep()
        }, time)
    }

    override fun showMessage(msg: String, withProgress: Boolean) {
        if (withProgress) progress.visibility = View.VISIBLE else progress.visibility = View.GONE
        textView.visibility = View.VISIBLE
        textView.text = msg
    }

    override fun applicationActivatedNextStep() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
