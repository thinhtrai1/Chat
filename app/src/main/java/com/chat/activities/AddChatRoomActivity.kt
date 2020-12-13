//package com.chat.activities
//
//import android.app.Activity
//import android.content.Intent
//import android.os.Bundle
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.chat.R
//import com.chat.adapters.UserRcvAdapter
//import com.chat.models.ChatRoom
//import com.chat.models.User
//import com.chat.utils.Constants
//import com.chat.utils.Utility
//import com.chat.views.SelectUserDialog
//import com.google.gson.Gson
//import kotlinx.android.synthetic.main.activity_add_chat_room.*
//import okhttp3.MediaType
//import okhttp3.RequestBody
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//class AddChatRoomActivity : BaseActivity() {
//    private val mMembers = ArrayList<User>()
//    private val mAdapter = UserRcvAdapter(this, mMembers, ArrayList(), false)
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_add_chat_room)
//
//        rcvMember.adapter = mAdapter
//        rcvMember.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
//
//        btnAddMember.setOnClickListener {
//            SelectUserDialog(this, mMembers, object : SelectUserDialog.IOnSelectedListener {
//                override fun onFinishSelected() {
//                    mAdapter.notifyDataSetChanged()
//                }
//
//                override fun onError(error: Any?) {
//                    if (error is Throwable) {
//                        showAlert(error)
//                    } else {
//                        showAlert(error.toString())
//                    }
//                }
//            }).show()
//        }
//
//        btnAdd.setOnClickListener {
//            showLoading(true)
//            val mediaType = MediaType.parse("text/plain")
//            Utility.apiClient.createRoom(
//                Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java).id,
//                RequestBody.create(mediaType, edtName.text.toString().trim()),
//                RequestBody.create(mediaType, Gson().toJson(mMembers.map { it.id }))
//                , null
//            ).enqueue(object : Callback<ChatRoom> {
//                override fun onFailure(call: Call<ChatRoom>, t: Throwable) {
//                    showAlert(t)
//                    showLoading(false)
//                }
//
//                override fun onResponse(call: Call<ChatRoom>, response: Response<ChatRoom>) {
//                    if (response.isSuccessful) {
//                        showToast(getString(R.string.created_room_successfully))
//                        setResult(Activity.RESULT_OK, Intent().putExtra("room", Gson().toJson(response.body())))
//                        finish()
//                    } else {
//                        showAlert(response.errorBody()?.string())
//                    }
//                    showLoading(false)
//                }
//            })
//        }
//    }
//}