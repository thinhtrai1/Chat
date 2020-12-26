package com.chat.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chat.R
import kotlinx.android.synthetic.main.progress_dialog.*
import java.io.IOException

abstract class BaseFragment : Fragment() {
    protected lateinit var mContext: Context
    private lateinit var mProgressDialog: Dialog
    private lateinit var mAlertDialog: AlertDialog.Builder

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mProgressDialog = Dialog(mContext)
        mProgressDialog.setContentView(R.layout.progress_dialog)
        mProgressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mProgressDialog.setCancelable(false)

        mAlertDialog = AlertDialog.Builder(mContext).setPositiveButton(android.R.string.ok, null)
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
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }

    protected fun showAlert(message: String?) {
        mAlertDialog.setMessage(message).show()
    }

    protected fun showAlert(throwable: Throwable?) {
        when {
            throwable is IOException -> {
                showAlert(getString(R.string.please_check_the_network_connection))
            }
            throwable?.message == null -> {
                showAlert(getString(R.string.an_error_occurred))
            }
            else -> {
                showAlert(throwable.message)
            }
        }
    }
}
