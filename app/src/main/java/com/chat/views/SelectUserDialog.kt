package com.chat.views

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat.R
import com.chat.activities.BaseActivity
import com.chat.adapters.UserRcvAdapter
import com.chat.models.User
import com.chat.utils.Utility
import kotlinx.android.synthetic.main.dialog_search_list_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectUserDialog(private val context: BaseActivity, selectedUser: ArrayList<User>, private val callback: IOnSelectedListener) : Dialog(context), Callback<ArrayList<User>> {
    private val memberList = ArrayList<User>()
    private val mAdapter = UserRcvAdapter(context, memberList, selectedUser, true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_search_list_user)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        rcvSelectMember.adapter = mAdapter
        rcvSelectMember.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        btnCancel.setOnClickListener {
            dismiss()
        }
        btnDone.setOnClickListener {
            dismiss()
            callback.onFinishSelected(mAdapter.selectedUser)
        }

        edtSearch.setOnEditorActionListener { textView, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                context.showLoading(true)
                Utility.apiClient.getUser(textView.text.toString()).enqueue(this)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    interface IOnSelectedListener {
        fun onFinishSelected(data: ArrayList<User>)
        fun onError(error: Any?)
    }

    override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
        context.showLoading(false)
        callback.onError(t)
    }

    override fun onResponse(call: Call<ArrayList<User>>, response: Response<ArrayList<User>>) {
        if (response.isSuccessful) {
            response.body()?.let {
                memberList.clear()
                memberList.addAll(it)
                mAdapter.notifyDataSetChanged()
            }
        } else {
            callback.onError(response.errorBody()?.string())
        }
        context.showLoading(false)
    }
}