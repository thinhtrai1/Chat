package com.chat.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.chat.R
import com.chat.fragments.ChatRoomFragment
import com.chat.models.User
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity() {
    private val mConstraintSet = ConstraintSet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val profile = Gson().fromJson(Utility.sharedPreferences.getString(Constants.PREF_USER, ""), User::class.java)
        tvName.text = profile.name
        tvEmail.text = profile.email
        if (profile.phone != null) {
            tvPhone.text = profile.phone
        } else {
            tvPhone.visibility = View.GONE
        }

        supportFragmentManager.beginTransaction().add(R.id.frameHome, ChatRoomFragment(profile.id)).commit()

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

        imvMenu.setOnClickListener {
            layoutContainer.openDrawer(viewMenuRight)
        }
        imvCloseDrawer.setOnClickListener {
            layoutContainer.closeDrawer(viewMenuRight)
        }

        viewAddChatRoom.setOnClickListener {
            layoutContainer.closeDrawer(viewMenuRight)
            startActivity(Intent(this, AddChatRoomActivity::class.java))
        }
    }
}