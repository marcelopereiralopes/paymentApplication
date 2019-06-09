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
import stone.providers.ActiveApplicationProvider
import stone.user.UserModel
import stone.utils.Stone
import stone.environment.Environment.SANDBOX

class ActiveApplicationCheckActivity : AppCompatActivity(), ActiveApplicationCheckView {

    private val activeApplicationCheckPresenter:
            ActiveApplicationCheckPresenter<ActiveApplicationCheckView> by lazy {
        ActiveApplicationCheckPresenterImpl(this)
    }

    private var userList: List<UserModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_application_check)
        userList = StoneStart.init(this)
        Stone.setEnvironment(SANDBOX)
        Stone.setAppName(getString(R.string.app_name))

        userList?.let {
            applicationActivatedNextStep()
        } ?: activeApplicationCheckPresenter.activeInvoke(ActiveApplicationProvider(this))
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
        AppStore.map["USER_LIST"] = userList
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
