package com.example.paymentapplication.view

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentapplication.R
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
            activeApplicationCheckPresenter.handleListData(it)
        } ?: activeApplicationCheckPresenter.handleEmptyListData(ActiveApplicationProvider(this))
    }

    override fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    override fun showMessage(msg: String, time: Long, withoutProgress: Boolean) {
        Handler().postDelayed({
            textView.visibility = View.VISIBLE
            textView.text = msg
            if (withoutProgress) progress.visibility = View.GONE
        }, time)

    }

}
