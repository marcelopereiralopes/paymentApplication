package com.example.paymentapplication.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentapplication.R
import com.example.paymentapplication.infrastructure.DispatcherProviderImpl
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
        ActiveApplicationCheckPresenterImpl(this, DispatcherProviderImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_application_check)

        Stone.setEnvironment(SANDBOX)
        Stone.setAppName(getString(R.string.app_name))

        val users = StoneStart.init(this)

        if (users != null && users.isNotEmpty()) applicationActivatedNextStep() else activateListener()

    }

    private fun activateListener() {
        activate.setOnClickListener {

            val code = stoneCode?.text?.toString() ?: ""

            if (code.length < 9)
                Toast.makeText(this, "Invalid StoneCode!", Toast.LENGTH_SHORT).show()
            else
                activeApplicationCheckPresenter.activeInvoke(
                    code,
                    ActiveApplicationProvider(this)
                )
        }
    }

    private fun hideKeyboard() {
        val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activate.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

    }

    override fun showProgress() {
        hideKeyboard()
        stoneCode.visibility = View.GONE
        activate.visibility = View.GONE
        progress.visibility = View.VISIBLE
        message.visibility = View.VISIBLE
    }

    override fun dismissProgress() {
        stoneCode.visibility = View.VISIBLE
        activate.visibility = View.VISIBLE
        progress.visibility = View.GONE
        message.visibility = View.GONE
        Toast.makeText(
            this, "Could not possible with this stone code!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun applicationActivatedNextStep() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
