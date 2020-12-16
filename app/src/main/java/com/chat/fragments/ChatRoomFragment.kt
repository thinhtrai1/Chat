package com.chat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.activities.HomeActivity
import com.chat.adapters.ChatRoomRcvAdapter
import com.chat.models.ChatRoom
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatRoomFragment : BaseFragment(), Callback<ArrayList<ChatRoom>> {
    private lateinit var mAdapter: ChatRoomRcvAdapter
    private val mChatRooms = ArrayList<ChatRoom>()
    private var mSearchKey = ""
    private var mCurrentPage = 0
    private val mCreateRoomCallback = object : CreateRoomFragment.IOnCreatedChatRoom {
        override fun onCreated(position: Int, room: ChatRoom) {
            if (position == -1) {
                mChatRooms.add(0, room)
                mAdapter.notifyDataSetChanged()
                showToast(getString(R.string.created_room_successfully))
            } else {
                mChatRooms[position] = room
                mAdapter.notifyDataSetChanged()
                showToast(getString(R.string.updated_room_successfully))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)
        mAdapter = ChatRoomRcvAdapter(mContext, mCreateRoomCallback, mChatRooms)
        (view as RecyclerView).adapter = mAdapter
        view.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        loadData(mSearchKey)
    }

    fun loadData(searchKey: String) {
        showLoading(true)
        mSearchKey = searchKey
        Utility.apiClient.getChatRoom(
            Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java).id,
            mSearchKey,
            mCurrentPage
        ).enqueue(this)
    }

    fun createRoom() {
        (mContext as HomeActivity).addFragment(CreateRoomFragment(mCreateRoomCallback, null))
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
                if (mCurrentPage == 0) {
                    mChatRooms.clear()
                }
                mChatRooms.addAll(it)
                mAdapter.notifyDataSetChanged()
            }
        } else {
            showAlert(response.errorBody()?.string())
        }
        showLoading(false)
    }
}