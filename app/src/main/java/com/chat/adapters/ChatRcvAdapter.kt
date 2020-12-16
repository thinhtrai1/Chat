package com.chat.adapters

import android.app.Activity
import android.app.Dialog
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.models.Chat
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.squareup.picasso.Picasso

class ChatRcvAdapter(
    private val mContext: Activity,
    private val mList: ArrayList<Chat>,
    private val mUserId: Int) : RecyclerView.Adapter<ChatRcvAdapter.ViewHolder>() {

    private var imageMinWidth = 0F

    init {
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= 29) {
            mContext.display?.getRealMetrics(metrics)
        } else {
            @Suppress("DEPRECATION")
            mContext.windowManager.defaultDisplay?.getRealMetrics(metrics)
        }
        imageMinWidth = metrics.widthPixels / 2F
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rcv_chat, parent, false))
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mList[position].let {
            holder.tvTime.text = Utility.getTimeDisplayString(it.time)
            when (it.type) {
                0 -> {
                    holder.tvMessage.text = it.message
                    holder.tvMessage.visibility = View.VISIBLE
                    holder.viewImage.visibility = View.GONE
                }
                1 -> {
                    Picasso.get().load(Constants.BASE_URL + it.message).into(holder.imageView)
                    holder.tvMessage.visibility = View.GONE
                    holder.viewImage.visibility = View.VISIBLE
                    holder.imageView.setOnClickListener { _ ->
                        Dialog(mContext, android.R.style.Theme_Black_NoTitleBar).apply {
                            val imageView = ImageView(mContext)
                            setContentView(imageView)
                            show()
                            Picasso.get().load(Constants.BASE_URL + it.message).into(imageView)
                        }
                    }
                }
                else -> {
                    holder.tvMessage.visibility = View.GONE
                    holder.viewImage.visibility = View.GONE
                }
            }

            if (it.senderId == mUserId) {
                holder.tvMessage.isSelected = true
                holder.tvName.visibility = View.INVISIBLE
                holder.viewSpaceLeft.visibility = View.VISIBLE
                holder.viewSpaceRight.visibility = View.GONE
            } else {
                holder.tvMessage.isSelected = false
                holder.tvName.visibility = View.VISIBLE
                holder.tvName.text = it.name
                holder.viewSpaceLeft.visibility = View.GONE
                holder.viewSpaceRight.visibility = View.VISIBLE
            }
        }
    }

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        internal val tvName: TextView = row.findViewById(R.id.tvName)
        internal val tvTime: TextView = row.findViewById(R.id.tv_time)
        internal val tvMessage: TextView = row.findViewById(R.id.tv_message)
        internal val viewSpaceLeft: View = row.findViewById(R.id.view_space_left)
        internal val viewSpaceRight: View = row.findViewById(R.id.view_space_right)
        internal val viewImage: CardView = row.findViewById(R.id.cardViewImage)
        internal val imageView: ImageView = row.findViewById(R.id.imageView)
    }
}