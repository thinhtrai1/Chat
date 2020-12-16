package com.chat.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.activities.ChatActivity
import com.chat.activities.HomeActivity
import com.chat.fragments.CreateRoomFragment
import com.chat.models.ChatRoom
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.squareup.picasso.Picasso

class ChatRoomRcvAdapter(private val mContext: Context, private val editCallback: CreateRoomFragment.IOnCreatedChatRoom, private val mRooms: ArrayList<ChatRoom>) :
    RecyclerView.Adapter<ChatRoomRcvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.item_rcv_chat_room, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mRooms.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mRooms[position].let {
            if (!it.image.isNullOrEmpty()) {
                Picasso.get().load(Constants.BASE_URL + it.image).placeholder(R.drawable.ic_app)
                    .resize(200, 200).centerCrop().into(holder.imageView)
            } else {
                holder.imageView.setImageResource(R.drawable.ic_app)
            }
            holder.tvName.text = it.name
            holder.tvLastMessage.text = it.lastMessage
            holder.tvTime.text = Utility.getTimeDisplayString(it.lastMessageTime)
            if (it.isHost == 1) {
                holder.imvSetting.visibility = View.VISIBLE
                holder.imvSetting.setOnClickListener { _ ->
                    (mContext as HomeActivity).addFragment(CreateRoomFragment(editCallback, it.id))
                }
            } else {
                holder.imvSetting.visibility = View.GONE
            }
            holder.imageView.setOnClickListener { _ ->
                if (it.isHost == 1) {
                    (mContext as HomeActivity).addFragment(CreateRoomFragment(editCallback, it.id))
                } else if (!it.image.isNullOrEmpty()) {
                    Dialog(mContext, android.R.style.Theme_Black_NoTitleBar).apply {
                        setContentView(ImageView(mContext).apply {
                            Picasso.get().load(Constants.BASE_URL + it.image).into(this)
                        })
                        show()
                    }
                }
            }
            holder.itemView.setOnClickListener {
                mContext.startActivity(Intent(mContext, ChatActivity::class.java))
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val imageView: ImageView = view.findViewById(R.id.imageView)
        internal val tvName: TextView = view.findViewById(R.id.tvName)
        internal val tvLastMessage: TextView = view.findViewById(R.id.tvMessage)
        internal val tvTime: TextView = view.findViewById(R.id.tvTime)
        internal val imvSetting: ImageView = view.findViewById(R.id.imvSetting)
    }
}