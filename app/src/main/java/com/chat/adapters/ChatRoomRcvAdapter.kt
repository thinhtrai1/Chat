package com.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.models.ChatRoom
import com.chat.utils.Constants
import com.chat.utils.Utility
import com.squareup.picasso.Picasso

class ChatRoomRcvAdapter(
    private val mContext: Context,
    private val onClickCallback: IOnItemClickListener,
    private val mRooms: ArrayList<ChatRoom>) :
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
            holder.tvTime.text = Utility.getTimeDisplayString(it.lastMessageTime)
            when (it.lastMessageType) {
                0 -> {
                    holder.tvLastMessage.text = it.lastMessage
                }
                1 -> {
                    holder.tvLastMessage.text = mContext.getString(R.string.image)
                }
                2 -> {
                    holder.tvLastMessage.text = mContext.getString(R.string.video)
                }
                3 -> {
                    holder.tvLastMessage.text = mContext.getString(R.string.audio)
                }
            }
            if (it.isHost) {
                holder.imvSetting.visibility = View.VISIBLE
            } else {
                holder.imvSetting.visibility = View.GONE
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val imageView: ImageView = view.findViewById(R.id.imageView)
        internal val tvName: TextView = view.findViewById(R.id.tvName)
        internal val tvLastMessage: TextView = view.findViewById(R.id.tvMessage)
        internal val tvTime: TextView = view.findViewById(R.id.tvTime)
        internal val imvSetting: ImageView = view.findViewById(R.id.imvSetting)

        init {
            view.setOnClickListener {
                onClickCallback.onClick(bindingAdapterPosition)
            }
        }
    }

    interface IOnItemClickListener {
        fun onClick(position: Int)
    }
}