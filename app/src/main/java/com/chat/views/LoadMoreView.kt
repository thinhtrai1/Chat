package com.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import androidx.core.content.res.ResourcesCompat
import com.chat.R

class LoadMoreView(context: Context, attr: AttributeSet?) : ProgressBar(context, attr) {

    init {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            indeterminateDrawable = ResourcesCompat.getDrawable(resources, R.drawable.progress_bar, null)
//        } else {
//            indeterminateDrawable = resources.getDrawable(R.drawable.progress_bar)
//        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        if (visibility == VISIBLE) {
            startAnimation(AnimationUtils.loadAnimation(context, R.anim.load_more_view_slide_up))
        }
        super.onVisibilityChanged(changedView, visibility)
    }
}