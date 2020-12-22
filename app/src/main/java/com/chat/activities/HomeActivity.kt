package com.chat.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.transition.ChangeBounds
import android.transition.TransitionManager
import androidx.appcompat.app.AppCompatDelegate
import com.chat.R
import com.chat.fragments.BaseFragment
import com.chat.fragments.ChatRoomFragment
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : BaseActivity(), View.OnClickListener {
    private val mConstraintSet = ConstraintSet()
    private val mFragmentManager = supportFragmentManager
    private var isSearch = false
    private lateinit var mDeviceId: String
    private lateinit var mUser: User
    private val mReceiverOpenRoomBroadcast: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            p1?.let { checkHandleIntent(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mFragmentManager.beginTransaction().replace(R.id.frameHome, ChatRoomFragment()).commit()
        mDeviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        mUser = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java)
        tvName.text = mUser.name
        tvEmail.text = mUser.email
        if (mUser.phone.isNullOrBlank()) {
            tvPhone.visibility = View.GONE
        } else {
            tvPhone.text = mUser.phone
        }
        if (mUser.image != null) {
            Picasso.get()
                .load(Constants.BASE_URL + mUser.image)
                .resize(200, 200)
                .centerCrop()
                .placeholder(R.drawable.ic_app)
                .into(imvAvatar)
        } else {
            imvAvatar.setImageResource(R.drawable.ic_app)
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            tvMode.text = getString(R.string.light_mode)
        } else {
            tvMode.text = getString(R.string.night_mode)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            Log.e("NEW_FCM_TOKEN", it.result ?: "")
            val userId = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java).id
            Utility.apiClient.updateFireBaseToken(userId, mDeviceId, it.result ?: "").enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                }
            })
        }

        edtSearch.setOnEditorActionListener { textView, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                (mFragmentManager.fragments[0] as ChatRoomFragment).loadData(textView.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        imvSearch.setOnClickListener(this)
        imvClose.setOnClickListener(this)
        imvMenu.setOnClickListener(this)
        imvCloseDrawer.setOnClickListener(this)
        viewAddChatRoom.setOnClickListener(this)
        viewUpdateProfile.setOnClickListener(this)
        viewMode.setOnClickListener(this)
        viewLogout.setOnClickListener(this)

        checkHandleIntent(intent)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(mReceiverOpenRoomBroadcast, IntentFilter(Constants.ACTION_OPEN_ROOM))
    }

    override fun onClick(p0: View?) {
        when (p0) {
            imvSearch -> {
                if (isSearch) {
                    (mFragmentManager.fragments[0] as ChatRoomFragment).loadData(edtSearch.text.toString())
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

            imvClose -> {
                mConstraintSet.clone(layoutProfile)
                mConstraintSet.clear(R.id.viewSearch, ConstraintSet.BOTTOM)
                mConstraintSet.connect(R.id.viewSearch, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                mConstraintSet.connect(R.id.viewProfile, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                mConstraintSet.connect(R.id.viewProfile, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                TransitionManager.beginDelayedTransition(layoutProfile, ChangeBounds().setDuration(100))
                mConstraintSet.applyTo(layoutProfile)
                edtSearch.setText("")
                isSearch = false
            }

            imvMenu -> {
                layoutContainer.openDrawer(viewMenuRight)
            }
            imvCloseDrawer -> {
                layoutContainer.closeDrawer(viewMenuRight)
            }

            viewAddChatRoom -> {
                layoutContainer.closeDrawer(viewMenuRight)
                if (mFragmentManager.fragments.last() is ChatRoomFragment) {
                    (mFragmentManager.fragments.last() as ChatRoomFragment).createRoom()
                }
            }

            viewUpdateProfile -> {
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java).putExtra("isUpdate", true))
            }

            viewMode -> {
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Utility.sharedPreferences.edit().putInt(Constants.PREF_DARK_MODE, AppCompatDelegate.MODE_NIGHT_NO).apply()
                    tvMode.text = getString(R.string.night_mode)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Utility.sharedPreferences.edit().putInt(Constants.PREF_DARK_MODE, AppCompatDelegate.MODE_NIGHT_YES).apply()
                    tvMode.text = getString(R.string.light_mode)
                }
            }

            viewLogout -> {
                Utility.sharedPreferences.edit().clear().apply()
                Utility.apiClient.logout(mUser.id, mDeviceId).enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {}
                })
                startActivity(Intent(this@HomeActivity, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    fun addFragment(fragment: BaseFragment) {
        mFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.trans_right_to_left_in, 0, 0, R.anim.trans_left_to_right_out)
            .add(R.id.frameHome, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private fun checkHandleIntent(intent: Intent) {
        intent.getStringExtra(Constants.EXTRA_ROOM_ID)?.let {
            startActivity(Intent(this, ChatActivity::class.java)
                .putExtra(Constants.EXTRA_ROOM_ID, it))
        }
    }

    private var isExit = false
    override fun finish() {
        if (isExit) {
            super.finish()
        } else {
            isExit = true
            Toast.makeText(this, getString(R.string.txt_tap_again_to_exit), Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({ isExit = false }, 2000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiverOpenRoomBroadcast)
    }
}