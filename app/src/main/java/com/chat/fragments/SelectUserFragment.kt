package com.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat.R
import com.chat.activities.BaseActivity
import com.chat.adapters.UserRcvAdapter
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_select_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectUserFragment(private val selectedUser: ArrayList<User>, private val callback: IOnSelectedListener) : BaseFragment(), Callback<ArrayList<User>> {
    private val memberList = ArrayList<User>()
    private lateinit var mAdapter: UserRcvAdapter
    private lateinit var mActivity: BaseActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = UserRcvAdapter(mContext, memberList, selectedUser, true)
        mActivity = mContext as BaseActivity

        rcvSelectMember.adapter = mAdapter
        rcvSelectMember.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        btnCancel.setOnClickListener {
            mActivity.supportFragmentManager.popBackStackImmediate()
        }
        btnDone.setOnClickListener {
            mActivity.supportFragmentManager.popBackStackImmediate()
            callback.onFinishSelected()
        }

        edtSearch.setOnEditorActionListener { textView, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                mActivity.showLoading(true)
                val userId = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java).id
                Utility.apiClient.getUser(userId, textView.text.toString()).enqueue(this)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    interface IOnSelectedListener {
        fun onFinishSelected()
        fun onError(error: Any?)
    }

    override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
        mActivity.showLoading(false)
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
        mActivity.showLoading(false)
    }
}