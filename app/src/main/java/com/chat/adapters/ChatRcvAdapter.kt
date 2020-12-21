package com.chat.adapters

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.models.Message
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.squareup.picasso.Picasso

class ChatRcvAdapter(
    private val mContext: Activity,
    private val mList: ArrayList<Message>,
    private val mUserId: Int) : RecyclerView.Adapter<ChatRcvAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_rcv_chat, parent, false))
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mList[position].let {
            when (it.type) {
                0 -> {
                    holder.tvMessage.text = it.message
                    holder.tvMessage.visibility = View.VISIBLE
                    holder.cardMessage.visibility = View.GONE
                }
                1 -> {
                    Picasso.get()
                        .load(Constants.BASE_URL + it.message)
                        .resizeDimen(R.dimen.message_image_height, R.dimen.message_image_height)
                        .centerInside()
                        .into(holder.imvMessage)
                    holder.tvMessage.visibility = View.GONE
                    holder.cardMessage.visibility = View.VISIBLE
                    holder.cardMessage.setOnClickListener { _ ->
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
                    holder.cardMessage.visibility = View.GONE
                }
            }

            if (it.senderId == mUserId) {
                holder.tvMessage.isSelected = true
                holder.tvTime1.visibility = View.GONE
                holder.groupMessage1.gravity = Gravity.END
                holder.groupMessage2.gravity = Gravity.END
                if (holder.adapterPosition > 0 && mList[holder.adapterPosition -1].senderId == mUserId && mList[holder.adapterPosition -1].time + 180000 > it.time) {
                    holder.tvName.visibility = View.GONE
                    holder.imvAvatar.visibility = View.GONE
                    holder.tvTime2.visibility = View.GONE
                } else {
                    holder.imvAvatar.visibility = View.INVISIBLE
                    holder.tvName.visibility = View.INVISIBLE
                    holder.tvTime2.visibility = View.VISIBLE
                    holder.tvTime2.text = Utility.getTimeDisplayString(it.time)
                }
            } else {
                holder.tvMessage.isSelected = false
                holder.tvTime2.visibility = View.GONE
                holder.groupMessage1.gravity = Gravity.START
                holder.groupMessage2.gravity = Gravity.START
                if (holder.adapterPosition > 0 && mList[holder.adapterPosition -1].senderId == it.senderId) {
                    if (mList[holder.adapterPosition -1].time + 180000 > it.time) {
                        holder.imvAvatar.visibility = View.GONE
                        holder.tvName.visibility = View.GONE
                        holder.tvTime1.visibility = View.GONE
                    } else {
                        holder.imvAvatar.visibility = View.INVISIBLE
                        holder.tvName.text = it.name
                        holder.tvName.visibility = View.INVISIBLE
                        holder.tvTime1.visibility = View.VISIBLE
                        holder.tvTime1.text = Utility.getTimeDisplayString(it.time)
                    }
                } else {
                    holder.tvName.visibility = View.VISIBLE
                    holder.tvName.text = it.name
                    holder.imvAvatar.visibility = View.VISIBLE
                    if (it.avatar.isNullOrEmpty()) {
                        holder.imvAvatar.setImageResource(R.drawable.ic_app)
                    } else {
                        Picasso.get()
                            .load(Constants.BASE_URL + it.avatar)
                            .resize(200, 200)
                            .centerCrop()
                            .placeholder(R.drawable.ic_app)
                            .into(holder.imvAvatar)
                    }
                    holder.tvTime1.visibility = View.VISIBLE
                    holder.tvTime1.text = Utility.getTimeDisplayString(it.time)
                }
            }
        }
    }

    inner class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        internal val tvName: TextView = row.findViewById(R.id.tvName)
        internal val imvAvatar: ImageView = row.findViewById(R.id.imvAvatar)
        internal val tvTime1: TextView = row.findViewById(R.id.tvTime1)
        internal val tvTime2: TextView = row.findViewById(R.id.tvTime2)
        internal val tvMessage: TextView = row.findViewById(R.id.tvMessage)
        internal val cardMessage: CardView = row.findViewById(R.id.cardMessage)
        internal val imvMessage: ImageView = row.findViewById(R.id.imvMessage)
        internal val groupMessage1: LinearLayout = row.findViewById(R.id.groupMessage1)
        internal val groupMessage2: LinearLayout = row.findViewById(R.id.groupMessage2)
    }
}