package com.chat.fragments

import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.activities.ChatActivity
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
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList

class ChatRoomFragment : BaseFragment(), Callback<ArrayList<ChatRoom>>, CreateRoomFragment.IOnCreatedChatRoom {
    private lateinit var mAdapter: ChatRoomRcvAdapter
    private val mChatRooms = ArrayList<ChatRoom>()
    private var mSearchKey = ""
    private var mCurrentPage = 0
    private var mCurrentPositionChat = 0
    private val mReceiverRoomBroadcast: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.let {
                try {
                    if (it.getStringExtra(Constants.EXTRA_ROOM_ID) != null) {
                        for (i in 0 until mChatRooms.size) {
                            if (mChatRooms[i].roomId.toString() == it.getStringExtra(Constants.EXTRA_ROOM_ID)) {
                                mChatRooms[i].lastMessage = it.getStringExtra(Constants.EXTRA_MESSAGE)
                                mChatRooms[i].lastMessageType = (it.getStringExtra(Constants.EXTRA_MESSAGE_TYPE) ?: "0").toInt()
                                mChatRooms[i].lastMessageTime = (it.getStringExtra(Constants.EXTRA_MESSAGE_TIME) ?: "0").toLong()
                                mAdapter.notifyItemChanged(i)

                                if (i != 0) {
                                    Collections.swap(mChatRooms, 0, i)
                                    mAdapter.notifyItemMoved(0, i)
                                }
                                break
                            }
                        }
                    }
                } catch (e: NumberFormatException) {}
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return RecyclerView(mContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading(true)
        mAdapter = ChatRoomRcvAdapter(mContext, object : CreateRoomFragment.IOnCreatedChatRoom {
            override fun onCreated(position: Int, room: ChatRoom) {
                startActivityForResult(Intent(mContext, ChatActivity::class.java)
                        .putExtra(Constants.EXTRA_ROOM, Gson().toJson(room)), 1997)
                mCurrentPositionChat = position
            }
        }, this, mChatRooms)
        (view as RecyclerView).adapter = mAdapter
        view.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)
        loadData(mSearchKey)

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiverRoomBroadcast, IntentFilter(Constants.ACTION_NEW_MESSAGE))
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

    fun createRoom() {
        (mContext as HomeActivity).addFragment(CreateRoomFragment(this, null))
    }

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

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiverRoomBroadcast)
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1997) {
                mChatRooms[mCurrentPositionChat].lastMessage = data.getStringExtra(Constants.EXTRA_MESSAGE)
                mChatRooms[mCurrentPositionChat].lastMessageType = (data.getStringExtra(Constants.EXTRA_MESSAGE_TYPE) ?: "0").toInt()
                mChatRooms[mCurrentPositionChat].lastMessageTime = (data.getStringExtra(Constants.EXTRA_MESSAGE_TIME) ?: "0").toLong()
                mAdapter.notifyItemChanged(mCurrentPositionChat)
                if (mCurrentPositionChat != 0) {
                    Collections.swap(mChatRooms, 0, mCurrentPositionChat)
                    mAdapter.notifyItemMoved(0, mCurrentPositionChat)
                    mCurrentPositionChat = 0
                }
            }
        }
    }
}