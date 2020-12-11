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

class ChatRoomRcvAdapter(private val context: Context, private val mRooms: ArrayList<ChatRoom>) :
    RecyclerView.Adapter<ChatRoomRcvAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_rcv_chat_room, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mRooms.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mRooms[position].let {
            if (it.image != null) {
                Picasso.get().load(Constants.BASE_URL + it.image).placeholder(R.drawable.ic_launcher_round)
                    .resize(200, 200).into(holder.imageView)
            } else {
                holder.imageView.setImageResource(R.drawable.ic_launcher_round)
            }
            holder.tvName.text = it.name
            holder.tvLastMessage.text = it.lastMessage
            holder.tvTime.text = Utility.getTime(it.lastMessageTime)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val imageView: ImageView = view.findViewById(R.id.imageView)
        internal val tvName: TextView = view.findViewById(R.id.tvName)
        internal val tvLastMessage: TextView = view.findViewById(R.id.tvMessage)
        internal val tvTime: TextView = view.findViewById(R.id.tvTime)
    }
}