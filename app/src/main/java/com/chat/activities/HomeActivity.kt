package com.chat.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.chat.R
import com.chat.fragments.BaseFragment
import com.chat.fragments.ChatRoomFragment
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity() {
    private val mConstraintSet = ConstraintSet()
    private val mFragmentManager = supportFragmentManager
    private var isSearch = false
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mFragmentManager.beginTransaction().replace(R.id.frameHome, ChatRoomFragment()).commit()

        mUser = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java)
        tvName.text = mUser.name
        tvEmail.text = mUser.email
        if (mUser.phone.isNullOrBlank()) {
            tvPhone.visibility = View.GONE
        } else {
            tvPhone.text = mUser.phone
        }
        if (mUser.image != null) {
            Picasso.get().load(Constants.BASE_URL + mUser.image).placeholder(R.drawable.ic_app).into(imvAvatar)
        } else {
            imvAvatar.setImageResource(R.drawable.ic_app)
        }

        imvSearch.setOnClickListener {
            if (isSearch) {
                (mFragmentManager.fragments[0] as ChatRoomFragment).loadData(edtSearch.text.toString())
            } else {
                mConstraintSet.clone(layoutProfile)
                mConstraintSet.connect(R.id.viewSearch, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                mConstraintSet.connect(R.id.viewSearch, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
                mConstraintSet.clear(R.id.viewProfile, ConstraintSet.TOP)
                mConstraintSet.connect(R.id.viewProfile, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
                TransitionManager.beginDelayedTransition(layoutProfile, ChangeBounds().setDuration(200))
                mConstraintSet.applyTo(layoutProfile)
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

        edtSearch.setOnEditorActionListener { textView, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEARCH) {
                (mFragmentManager.fragments[0] as ChatRoomFragment).loadData(textView.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        imvMenu.setOnClickListener {
            layoutContainer.openDrawer(viewMenuRight)
        }
        imvCloseDrawer.setOnClickListener {
            layoutContainer.closeDrawer(viewMenuRight)
        }

        viewAddChatRoom.setOnClickListener {
            layoutContainer.closeDrawer(viewMenuRight)
            if (mFragmentManager.fragments.last() is ChatRoomFragment) {
                (mFragmentManager.fragments.last() as ChatRoomFragment).createRoom()
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

    private var isExit = false
//    override fun onBackPressed() {
//        if (isExit) {
//            super.onBackPressed()
//        } else {
//            isExit = true
//            Toast.makeText(this, getString(R.string.txt_tap_again_to_exit), Toast.LENGTH_SHORT).show()
//            Handler(Looper.getMainLooper()).postDelayed({ isExit = false }, 2000)
//        }
//    }

    override fun finish() {
        if (isExit) {
        super.finish()
        } else {
            isExit = true
            Toast.makeText(this, getString(R.string.txt_tap_again_to_exit), Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({ isExit = false }, 2000)
        }
    }
}