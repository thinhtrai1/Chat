package com.chat.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.chat.R
import com.chat.activities.HomeActivity
import com.chat.adapters.UserRcvAdapter
import com.chat.models.ChatRoom
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_create_chat_room.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreateRoomFragment: BaseFragment() {
    private var mRoomId = -1
    private val mMembers = ArrayList<User>()
    private var imageUri: Uri? = null
    private lateinit var mAdapter: UserRcvAdapter
    private lateinit var mCallback: IOnCreatedChatRoom

    companion object {
        fun newInstance(callback: IOnCreatedChatRoom, roomId: Int): CreateRoomFragment {
            val bundle = Bundle()
            bundle.putParcelable(CALLBACK, callback)
            bundle.putInt(ROOM_ID, roomId)
            return CreateRoomFragment().apply {
                arguments = bundle
            }
        }

        private const val CALLBACK = "CALLBACK"
        private const val ROOM_ID = "ROOM_ID"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_create_chat_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCallback = arguments?.getParcelable<IOnCreatedChatRoom>(CALLBACK) as IOnCreatedChatRoom
        mRoomId = arguments?.getInt(ROOM_ID) ?: -1

        mAdapter = UserRcvAdapter(mContext, mMembers, ArrayList(), false)
        rcvMember.adapter = mAdapter
        rcvMember.layoutManager = LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false)

        imvAddAvatar.setOnClickListener {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                }
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }

        imvAddMember.setOnClickListener {
            (activity as HomeActivity).addFragment(
                SelectUserFragment.newInstance(
                    mMembers,
                    object : SelectUserFragment.IOnSelectedListener {
                        override fun onFinishSelected() {
                            mAdapter.notifyDataSetChanged()
                        }

                        override fun onError(error: Any?) {
                            if (error is Throwable) {
                                showAlert(error)
                            } else {
                                showAlert(error.toString())
                            }
                        }

                        override fun writeToParcel(p0: Parcel?, p1: Int) {
                        }

                        override fun describeContents(): Int {
                            return 0
                        }
                    })
            )
        }

        btnCreate.setOnClickListener {
            if (edtName.text.isBlank()) {
                edtName.error = getString(R.string.tv_please_enter_name)
                return@setOnClickListener
            }
            showLoading(true)
            var imageFile: MultipartBody.Part? = null
            imageUri?.getRealPath()?.let {
                val avatarRequest = RequestBody.create(MediaType.parse("image/*"), it)
                imageFile = MultipartBody.Part.createFormData("image", it.name, avatarRequest)
            }
            val mediaType = MediaType.parse("text/plain")
            Utility.apiClient.createRoom(
                Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java).id,
                RequestBody.create(mediaType, edtName.text.toString().trim()),
                RequestBody.create(mediaType, Gson().toJson(mMembers.map { it.id })),
                imageFile
            ).enqueue(object : Callback<ChatRoom> {
                override fun onFailure(call: Call<ChatRoom>, t: Throwable) {
                    showAlert(t)
                    showLoading(false)
                }

                override fun onResponse(call: Call<ChatRoom>, response: Response<ChatRoom>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            mCallback.onCreated(-1, it)
                            activity?.supportFragmentManager?.popBackStackImmediate()
                        }
                    } else {
                        showAlert(response.errorBody()?.string())
                    }
                    showLoading(false)
                }
            })
        }

        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStackImmediate()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 0) {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 0)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            imvAvatar.setImageURI(data.data)
            imvAvatar.alpha = 1F
            imvAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            imvAddAvatar.visibility = View.GONE
            imageUri = data.data
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun Uri?.getRealPath(): File? {
        if (this != null) {
            val data = MediaStore.Images.Media.DATA
            mContext.contentResolver.query(this, arrayOf(data), null, null, null)?.apply {
                moveToFirst()
                return File(getString(getColumnIndexOrThrow(data)))
            }
        }
        return null
    }

    interface IOnCreatedChatRoom: Parcelable {
        fun onCreated(position: Int, room: ChatRoom)
    }
}