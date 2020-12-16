package com.chat.activities

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.chat.R
import com.chat.adapters.ChatRcvAdapter
import com.chat.models.Chat
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : BaseActivity() {
    private var isSearch = false
    private val mConstraintSet = ConstraintSet()
    private val mChatList = ArrayList<Chat>()
    private lateinit var mAdapter: ChatRcvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mAdapter = ChatRcvAdapter(this, mChatList, Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java).id)
        rcvChat.adapter = mAdapter
        rcvChat.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        imvSearch.setOnClickListener {
            if (isSearch) {
                loadData()
            } else {
                mConstraintSet.clone(layoutProfile)
                mConstraintSet.connect(R.id.viewSearch, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                mConstraintSet.connect(R.id.viewSearch, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                mConstraintSet.clear(R.id.viewProfile, ConstraintSet.TOP)
                mConstraintSet.connect(R.id.viewProfile, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                TransitionManager.beginDelayedTransition(layoutProfile, ChangeBounds().setDuration(200))
                mConstraintSet.applyTo(layoutProfile)
                edtSearch.requestFocus()
                isSearch = true
            }
        }

        imvClose.setOnClickListener {
            mConstraintSet.clone(layoutProfile)
            mConstraintSet.clear(R.id.viewSearch, ConstraintSet.BOTTOM)
            mConstraintSet.connect(R.id.viewSearch, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            mConstraintSet.connect(R.id.viewProfile, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            mConstraintSet.connect(R.id.viewProfile, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            TransitionManager.beginDelayedTransition(layoutProfile, ChangeBounds().setDuration(200))
            mConstraintSet.applyTo(layoutProfile)
            edtSearch.setText("")
            isSearch = false
        }
    }

    private fun loadData() {}
}