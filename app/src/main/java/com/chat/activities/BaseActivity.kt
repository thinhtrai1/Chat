package com.chat.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chat.R
import kotlinx.android.synthetic.main.progress_dialog.*
import java.io.IOException

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var mProgressDialog: Dialog
    private lateinit var mAlertDialog: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.progress_dialog)
        mProgressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mProgressDialog.setCancelable(false)

        mAlertDialog = AlertDialog.Builder(this).setPositiveButton(android.R.string.ok, null)
    }

    protected fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            mProgressDialog.tvMessage.text = getString(R.string.loading)
            mProgressDialog.show()
        } else {
            mProgressDialog.dismiss()
        }
    }

    protected fun showLoading(message: String?) {
        mProgressDialog.tvMessage.text = message
        mProgressDialog.show()
    }

    protected fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showAlert(message: String?) {
        mAlertDialog.setMessage(message).show()
    }

    protected fun showAlert(throwable: Throwable?) {
        when {
            throwable is IOException -> {
                showAlert(getString(R.string.please_check_the_network_connection))
            }
            throwable?.message != null -> {
                showAlert(getString(R.string.an_error_occurred_message, throwable.message))
            }
            else -> {
                showAlert(getString(R.string.an_error_occurred))
            }
        }
    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.anim_scale_open, R.anim.anim_scale_exit)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.anim_scale_open, R.anim.anim_scale_exit)
    }
}