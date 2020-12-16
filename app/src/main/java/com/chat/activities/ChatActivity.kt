package com.chat.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.chat.R
import com.chat.adapters.ChatRcvAdapter
import com.chat.models.Chat
import com.chat.models.ChatRoom
import com.chat.models.User
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

class ChatActivity : BaseActivity(), Callback<Chat> {
    companion object {
        private const val IMAGE_LIBRARY_REQUEST_CODE = 1001
        private const val IMAGE_CAMERA_REQUEST_CODE = 1002
        private const val VIDEO_LIBRARY_REQUEST_CODE = 1003
        private const val VIDEO_CAMERA_REQUEST_CODE = 1004
        private const val AUDIO_LIBRARY_REQUEST_CODE = 1005
        private const val AUDIO_CAMERA_REQUEST_CODE = 1006
    }

    private val mUser: User = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java)
    private var isSearch = false
    private val mConstraintSet = ConstraintSet()
    private val mChatList = ArrayList<Chat>()
    private lateinit var mAdapter: ChatRcvAdapter
    private lateinit var mRoom: ChatRoom
    private val mGetMessageCallback: Callback<ArrayList<Chat>> = object : Callback<ArrayList<Chat>> {
        override fun onFailure(call: Call<ArrayList<Chat>>, t: Throwable) {
            showLoading(false)
            showAlert(t)
        }

        override fun onResponse(call: Call<ArrayList<Chat>>, response: Response<ArrayList<Chat>>) {
            showLoading(false)
            if (response.isSuccessful) {
                response.body()?.let {
                    mChatList.addAll(0, it)
                    mAdapter.notifyDataSetChanged()
                }
            } else {
                showAlert(response.errorBody()?.string())
            }
        }
    }

    private fun loadData() {
        showLoading(true)
        Utility.apiClient.getMessage(
            mUser.id,
            mRoom.id,
            edtSearch.text.toString(),
            if (mChatList.isNotEmpty()) mChatList[0].time else 0
        ).enqueue(mGetMessageCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        mRoom = Gson().fromJson(intent.getStringExtra("room"), ChatRoom::class.java)
        tvName.text = mRoom.name
        Picasso.get().load(Constants.BASE_URL + mRoom.image).placeholder(R.drawable.ic_app)
            .resize(200, 200).centerCrop().into(imvAvatar)

        mAdapter = ChatRcvAdapter(this, mChatList, mUser.id)
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
            loadData()
        }

        edtMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
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
                        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                    }
                }
            } else {
                sendMessage(0, edtMessage.text.toString().trim(), null)
            }
        }
    }

    private fun sendMessage(type: Int, message: String?, file: File?) {
        val mediaType: MediaType? = MediaType.parse("text/plain")
        var messageData: RequestBody? = null
        var fileData: MultipartBody.Part? = null
        if (type == 0 && message != null) {
            messageData = RequestBody.create(mediaType, message)
        } else if (type != 0 && file != null) {
            val mediaFileType: RequestBody = RequestBody.create(MediaType.parse("image/*"), file)
            fileData = MultipartBody.Part.createFormData("file", file.name, mediaFileType)
        }

        showLoading(true)
        Utility.apiClient.sendMessage(
            RequestBody.create(mediaType, mUser.id.toString()),
            RequestBody.create(mediaType, mRoom.id.toString()),
            RequestBody.create(mediaType, type.toString()),
            messageData,
            fileData
        ).enqueue(this)
    }

    override fun onResponse(call: Call<Chat>, response: Response<Chat>) {
        showLoading(false)
        if (response.isSuccessful) {
            response.body()?.let {
                mChatList.add(it)
                mAdapter.notifyItemInserted(mChatList.lastIndex)
                rcvChat.scrollToPosition(mChatList.lastIndex)
                if (it.type == 0) {
                    edtMessage.setText("")
                }
            }
        } else {
            showAlert(response.errorBody()?.string())
        }
    }

    override fun onFailure(call: Call<Chat>, t: Throwable) {
        showLoading(false)
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
}