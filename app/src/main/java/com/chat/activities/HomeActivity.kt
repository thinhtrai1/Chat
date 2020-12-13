package com.chat.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.chat.R
import com.chat.fragments.BaseFragment
import com.chat.fragments.ChatRoomFragment
import com.chat.fragments.CreateRoomFragment
import com.chat.models.ChatRoom
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity() {
    private val mConstraintSet = ConstraintSet()
    private val mFragmentManager = supportFragmentManager
    private lateinit var mUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mUser = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java)
        tvName.text = mUser.name
        tvEmail.text = mUser.email
        if (mUser.phone.isNullOrBlank()) {
            tvPhone.visibility = View.GONE
        } else {
            tvPhone.text = mUser.phone
        }
        if (mUser.image != null) {
            Picasso.get().load(Constants.BASE_URL + mUser.image).into(imvAvatar)
        } else {
            imvAvatar.setImageResource(R.drawable.ic_app)
        }

        addFragment(ChatRoomFragment(mUser.id))

        imvSearch.setOnClickListener {
            mConstraintSet.clone(layoutProfile)
            if (imvSearch.drawable.constantState == ResourcesCompat.getDrawable(resources, R.drawable.ic_search, null)?.constantState) {
                mConstraintSet.connect(R.id.edtSearch, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                mConstraintSet.connect(R.id.viewProfile, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.START)
                imvSearch.setImageResource(R.drawable.ic_close)
            } else {
                imvSearch.setImageResource(R.drawable.ic_search)
                mConstraintSet.connect(R.id.edtSearch, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END)
                mConstraintSet.connect(R.id.viewProfile, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            }
            TransitionManager.beginDelayedTransition(layoutProfile, ChangeBounds().setInterpolator(
                AnticipateOvershootInterpolator(1.0f)
            ))
            mConstraintSet.applyTo(layoutProfile)
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
            if (mFragmentManager.fragments.last() !is CreateRoomFragment) {
                addFragment(CreateRoomFragment(mUser.id, object : CreateRoomFragment.IOnCreatedChatRoom {
                    override fun onCreated(room: ChatRoom) {
                        mFragmentManager.popBackStackImmediate()
                        (mFragmentManager.fragments[0] as ChatRoomFragment).addData(room)
                        showToast(getString(R.string.created_room_successfully))
                    }
                }))
            }
        }
    }

    private fun addFragment(fragment: BaseFragment) {
        mFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.trans_right_to_left_in, 0, 0, R.anim.trans_left_to_right_out)
            .add(R.id.frameHome, fragment)
            .addToBackStack(fragment.javaClass.name)
            .commit()
    }

    private var isExit = false
    override fun onBackPressed() {
        if (isExit) {
            finish()
        } else {
            isExit = true
            Toast.makeText(this, getString(R.string.txt_tap_again_to_exit), Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({ isExit = false }, 2000)
        }
    }
}