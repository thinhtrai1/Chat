package com.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chat.adapters.ChatRoomRcvAdapter
import com.chat.models.ChatRoom
import com.chat.utils.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatRoomFragment(private val userId: Int) : BaseFragment(), Callback<ArrayList<ChatRoom>> {
    private lateinit var mAdapter: ChatRoomRcvAdapter
    private val mChatRooms = ArrayList<ChatRoom>()
    private var mSearchKey = ""
    private var mCurrentPage = 0

    override fun initLayout(): Int {
        return 0
    }

    override fun initComponents() {
        showLoading(true)
        mAdapter = ChatRoomRcvAdapter(mContext, mChatRooms)
        Utility.apiClient.getChatRoom(userId, mSearchKey, mCurrentPage).enqueue(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return RecyclerView(mContext)
    }

    override fun onFailure(call: Call<ArrayList<ChatRoom>>, t: Throwable) {
        showLoading(false)
        showAlert(t)
    }

    override fun onResponse(call: Call<ArrayList<ChatRoom>>, response: Response<ArrayList<ChatRoom>>) {
        if (response.isSuccessful) {
            response.body()?.let {
                mChatRooms.addAll(it)
                mAdapter.notifyDataSetChanged()
            }
        } else {
            showAlert(response.errorBody()?.string())
        }
        showLoading(false)
    }
}