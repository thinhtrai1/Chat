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
import java.io.Serializable

class SelectUserFragment : BaseFragment(), Callback<ArrayList<User>> {
    private val mMemberList = ArrayList<User>()
    private lateinit var mAdapter: UserRcvAdapter
    private lateinit var mActivity: BaseActivity
    private lateinit var mSelectedUser: ArrayList<User>
    private lateinit var mCallback: IOnSelectedListener

    companion object {
        fun newInstance(selectedUser: ArrayList<User>, callback: IOnSelectedListener): SelectUserFragment {
            val bundle = Bundle()
            bundle.putSerializable(SELECTED_USER, selectedUser)
            bundle.putSerializable(CALLBACK, callback)
            return SelectUserFragment().apply {
                arguments = bundle
            }
        }

        private const val SELECTED_USER = "SELECTED_USER"
        private const val CALLBACK = "CALLBACK"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCallback = arguments?.getSerializable(CALLBACK) as IOnSelectedListener
        mSelectedUser = arguments?.getSerializable(SELECTED_USER) as ArrayList<User>
        mAdapter = UserRcvAdapter(mContext, mMemberList, mSelectedUser, true)
        mActivity = mContext as BaseActivity

        rcvSelectMember.adapter = mAdapter
        rcvSelectMember.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        btnCancel.setOnClickListener {
            mActivity.supportFragmentManager.popBackStackImmediate()
        }
        btnDone.setOnClickListener {
            mActivity.supportFragmentManager.popBackStackImmediate()
            mCallback.onFinishSelected()
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

    interface IOnSelectedListener: Serializable {
        fun onFinishSelected()
        fun onError(error: Any?)
    }

    override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
        mActivity.showLoading(false)
        mCallback.onError(t)
    }

    override fun onResponse(call: Call<ArrayList<User>>, response: Response<ArrayList<User>>) {
        if (response.isSuccessful) {
            response.body()?.let {
                mMemberList.clear()
                mMemberList.addAll(it)
                mAdapter.notifyDataSetChanged()
            }
        } else {
            mCallback.onError(response.errorBody()?.string())
        }
        mActivity.showLoading(false)
    }
}