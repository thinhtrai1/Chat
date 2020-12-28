package com.chat.activities

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.adapters.ChatRcvAdapter
import com.chat.models.Message
import com.chat.models.ChatRoom
import com.chat.models.User
import com.chat.services.ChatMessagingService
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.NumberFormatException

class ChatActivity : BaseActivity(), Callback<Message> {
    companion object {
        private const val IMAGE_LIBRARY_REQUEST_CODE = 1001
        private const val IMAGE_CAMERA_REQUEST_CODE = 1002
        private const val VIDEO_LIBRARY_REQUEST_CODE = 1003
        private const val VIDEO_CAMERA_REQUEST_CODE = 1004
        private const val AUDIO_LIBRARY_REQUEST_CODE = 1005
        private const val AUDIO_CAMERA_REQUEST_CODE = 1006
    }

    private lateinit var mAdapter: ChatRcvAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private val mUser: User = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java)
    private var isSearch = false
    private val mConstraintSet = ConstraintSet()
    private val mMessageList = ArrayList<Message>()
    private var mEarliestTime = 0L
    private var isLoadMore = false
    private var mRoom = ChatRoom()
    private var isTwoCallbackLoaded = false
    private val mGetMessageCallback: Callback<ArrayList<Message>> = object : Callback<ArrayList<Message>> {
        override fun onFailure(call: Call<ArrayList<Message>>, t: Throwable) {
            loadMoreView.visibility = View.GONE
            if (isTwoCallbackLoaded || mRoom.name != null) {
                showLoading(false)
            } else {
                isTwoCallbackLoaded = true
            }
            showAlert(t)
        }

        override fun onResponse(call: Call<ArrayList<Message>>, response: Response<ArrayList<Message>>) {
            loadMoreView.visibility = View.GONE
            if (isTwoCallbackLoaded || mRoom.name != null) {
                showLoading(false)
            } else {
                isTwoCallbackLoaded = true
            }
            if (response.isSuccessful) {
                response.body()?.let {
                    if (mEarliestTime == 0L) {
                        mMessageList.clear()
                        mMessageList.addAll(it)
                        mAdapter.notifyDataSetChanged()
                        if (it.size > 0) {
                            rcvChat.scrollToPosition(it.lastIndex)
                            isLoadMore = it[0].isLoadMore
                        }
                    } else {
                        mMessageList.addAll(0, it)
                        mAdapter.notifyItemRangeInserted(0, it.size)
                        mAdapter.notifyItemChanged(it.size)
                        if (it.size > 0) {
                            mLayoutManager.scrollToPositionWithOffset(it.lastIndex, 0)
                            isLoadMore = it[0].isLoadMore
                        }
                    }
                }
            } else {
                showAlert(response.errorBody()?.string())
            }
        }
    }
    private val mReceiverMessageBroadcast: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.let {
                try {
                    if ((it.getStringExtra(Constants.EXTRA_ROOM_ID) ?: "-1").toInt() == mRoom.roomId) {
                        val message = Message().apply {
                            id = (it.getStringExtra(Constants.EXTRA_MESSAGE_ID) ?: "-1").toInt()
                            message = it.getStringExtra(Constants.EXTRA_MESSAGE)
                            type = (it.getStringExtra(Constants.EXTRA_MESSAGE_TYPE) ?: "0").toInt()
                            time = (it.getStringExtra(Constants.EXTRA_MESSAGE_TIME) ?: "0").toLong()
                            senderId = (it.getStringExtra(Constants.EXTRA_SENDER_ID) ?: "-1").toInt()
                            name = it.getStringExtra(Constants.EXTRA_SENDER_NAME)
                            avatar = it.getStringExtra(Constants.EXTRA_SENDER_IMAGE)
                        }
                        mMessageList.add(message)
                        mAdapter.notifyItemInserted(mMessageList.lastIndex)
                        if (mLayoutManager.findLastVisibleItemPosition() > mMessageList.size - 3) {
                            mLayoutManager.scrollToPositionWithOffset(mMessageList.lastIndex, 0)
                        }
                    }
                } catch (e: NumberFormatException) {}
            }
        }
    }
    private val mReceiverOpenRoomBroadcast: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.let { finish() }
        }
    }

    private fun loadData() {
        if (mEarliestTime == 0L) {
            showLoading(true)
        } else {
            loadMoreView.visibility = View.VISIBLE
        }
        isLoadMore = false
        Utility.apiClient.getMessage(
            mUser.id,
            mRoom.roomId,
            edtSearch.text.toString(),
            mEarliestTime
        ).enqueue(mGetMessageCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mAdapter = ChatRcvAdapter(this, mMessageList, mUser.id)
        mLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rcvChat.adapter = mAdapter
        rcvChat.layoutManager = mLayoutManager

        if (intent.getStringExtra(Constants.EXTRA_ROOM_ID) == null && intent.getStringExtra(Constants.EXTRA_ROOM) == null) {
            showToast(getString(R.string.data_error))
            finish()
            return
        }

        if (intent.getStringExtra(Constants.EXTRA_ROOM_ID) != null) {
            try {
                mRoom.roomId = intent.getStringExtra(Constants.EXTRA_ROOM_ID)!!.toInt()
            } catch (e: NumberFormatException) {
                showToast(getString(R.string.data_error))
                finish()
                return
            }
        } else if (intent.getStringExtra(Constants.EXTRA_ROOM) != null) {
            mRoom = Gson().fromJson(intent.getStringExtra(Constants.EXTRA_ROOM), ChatRoom::class.java)
            tvName.text = mRoom.name
            Picasso.get().load(Constants.BASE_URL + mRoom.image).placeholder(R.drawable.ic_app)
                .resize(200, 200).centerCrop().into(imvAvatar)
        }

        Utility.apiClient.getDetailRoom(mUser.id, mRoom.roomId).enqueue(object : Callback<ChatRoom> {
            override fun onFailure(call: Call<ChatRoom>, t: Throwable) {
                if (isTwoCallbackLoaded) {
                    showLoading(false)
                } else {
                    isTwoCallbackLoaded = true
                }
                showAlert(t)
            }

            override fun onResponse(call: Call<ChatRoom>, response: Response<ChatRoom>) {
                if (isTwoCallbackLoaded) {
                    showLoading(false)
                } else {
                    isTwoCallbackLoaded = true
                }
                if (response.isSuccessful) {
                    response.body()?.let {
                        mRoom = it
                        ChatMessagingService.CURRENT_ROOM_ID = it.roomId
                        tvName.text = mRoom.name
                        if (it.member.size > 1) {
                            tvMemberNumber.visibility = View.VISIBLE
                            tvMemberNumber.text = getString(R.string.has_member, it.member.size)
                        }
                        if (it.isHost) {
                            imvMenu.visibility = View.VISIBLE
                        }
                        Picasso.get().load(Constants.BASE_URL + mRoom.image).placeholder(R.drawable.ic_app)
                            .resize(200, 200).centerCrop().into(imvAvatar)
                        LocalBroadcastManager.getInstance(this@ChatActivity)
                            .registerReceiver(mReceiverMessageBroadcast, IntentFilter(Constants.ACTION_NEW_MESSAGE))
                    }
                } else {
                    showAlert(response.errorBody()?.string())
                }
            }
        })

        loadData()
        listener()
    }

    private fun listener() {
        imvMenu.setOnClickListener {  }

        imvSearch.setOnClickListener {
            if (isSearch) {
                mEarliestTime = 0
                loadData()
            } else {
                mConstraintSet.clone(layoutProfile)
                mConstraintSet.connect(R.id.viewSearch, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                mConstraintSet.connect(R.id.viewSearch, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                mConstraintSet.clear(R.id.viewProfile, ConstraintSet.TOP)
                mConstraintSet.connect(R.id.viewProfile, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                TransitionManager.beginDelayedTransition(layoutProfile, ChangeBounds().setDuration(100))
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
            TransitionManager.beginDelayedTransition(layoutProfile, ChangeBounds().setDuration(100))
            mConstraintSet.applyTo(layoutProfile)
            edtSearch.setText("")
            isSearch = false
            mEarliestTime = 0
            loadData()
        }

        edtSearch.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                mEarliestTime = 0
                loadData()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        edtMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotBlank()) {
                    imvSend.setImageResource(R.drawable.ic_send_message)
                } else {
                    imvSend.setImageResource(R.drawable.ic_photo)
                }
            }
        })

        imvSend.setOnClickListener {
            if (edtMessage.text.toString().isBlank()) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, IMAGE_LIBRARY_REQUEST_CODE)
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), IMAGE_LIBRARY_REQUEST_CODE)
                    }
                }
            } else {
                sendMessage(0, edtMessage.text.toString().trim(), null)
            }
        }

        rcvChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                rcvChat.post {
                    rcvChat.smoothScrollToPosition(mMessageList.lastIndex)
                }
            }
        }

        rcvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoadMore && mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                    mEarliestTime = mMessageList[0].time
                    loadData()
                }
            }
        })
    }

    private fun sendMessage(type: Int, message: String?, file: File?) {
        imvSend.visibility = View.GONE
        progressBarSend.visibility = View.VISIBLE
        val mediaType: MediaType? = MediaType.parse("text/plain")
        var messageData: RequestBody? = null
        var fileData: MultipartBody.Part? = null
        if (type == 0 && message != null) {
            messageData = RequestBody.create(mediaType, message)
        } else if (type != 0 && file != null) {
            val mediaFileType: RequestBody = RequestBody.create(MediaType.parse("image/*"), file)
            fileData = MultipartBody.Part.createFormData("file", file.name, mediaFileType)
        }
        Utility.apiClient.sendMessage(
            RequestBody.create(mediaType, mUser.id.toString()),
            RequestBody.create(mediaType, mRoom.roomId.toString()),
            RequestBody.create(mediaType, type.toString()),
            messageData,
            fileData
        ).enqueue(this)
    }

    override fun onResponse(call: Call<Message>, response: Response<Message>) {
        imvSend.visibility = View.VISIBLE
        progressBarSend.visibility = View.GONE
        if (response.isSuccessful) {
            response.body()?.let {
                mMessageList.add(it)
                mAdapter.notifyItemInserted(mMessageList.lastIndex)
                if (mLayoutManager.findLastVisibleItemPosition() > mMessageList.size - 3) {
                    mLayoutManager.scrollToPositionWithOffset(mMessageList.lastIndex, 0)
                }
                if (it.type == 0) {
                    edtMessage.setText("")
                }
            }
        } else {
            showAlert(response.errorBody()?.string())
        }
    }

    override fun onFailure(call: Call<Message>, t: Throwable) {
        imvSend.visibility = View.VISIBLE
        progressBarSend.visibility = View.GONE
        showAlert(t)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == IMAGE_LIBRARY_REQUEST_CODE) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                IMAGE_LIBRARY_REQUEST_CODE -> {
                    sendMessage(1, null, data.data.getRealPath())
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun Uri?.getRealPath(): File? {
        if (this != null) {
            val data = MediaStore.Images.Media.DATA
            contentResolver.query(this, arrayOf(data), null, null, null)?.apply {
                moveToFirst()
                return File(getString(getColumnIndexOrThrow(data)))
            }
        }
        return null
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiverOpenRoomBroadcast, IntentFilter(Constants.ACTION_OPEN_ROOM))
    }

    override fun onDestroy() {
        ChatMessagingService.CURRENT_ROOM_ID = -1
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverMessageBroadcast)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverOpenRoomBroadcast)
        super.onDestroy()
    }

    override fun finish() {
        mMessageList.lastOrNull()?.let {
            setResult(Activity.RESULT_OK, Intent()
                .putExtra(Constants.EXTRA_MESSAGE, it.message)
                .putExtra(Constants.EXTRA_MESSAGE_TYPE, it.type)
                .putExtra(Constants.EXTRA_MESSAGE_TIME, it.time))
        }
        super.finish()
    }
}