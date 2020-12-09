package com.chat.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.chat.R
import java.io.IOException

abstract class BaseFragment : Fragment() {
    lateinit var mContext: Context
    private lateinit var mProgressDialog: Dialog
    private lateinit var mAlertDialog: AlertDialog.Builder

    protected abstract fun initLayout(): Int
    protected abstract fun initComponents()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(initLayout(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mProgressDialog = Dialog(mContext)
        mProgressDialog.setContentView(ProgressBar(mContext))
        mProgressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        mProgressDialog.setCancelable(false)

        mAlertDialog = AlertDialog.Builder(mContext).setPositiveButton(android.R.string.ok, null)

        initComponents()
    }

    protected fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            mProgressDialog.show()
        } else {
            mProgressDialog.dismiss()
        }
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
