package com.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chat.R
import com.chat.models.User
import com.chat.utils.Constants
import com.squareup.picasso.Picasso

class UserRcvAdapter(private val context: Context,
                     private val mUsers: ArrayList<User>,
                     private val selectedUser: ArrayList<User>,
                     private val isSelected: Boolean) : RecyclerView.Adapter<UserRcvAdapter.ViewHolder>() {

    private val selectedIds = selectedUser.map { it.id } as ArrayList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_rcv_user, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mUsers[position].let {
            if (it.image != null) {
                Picasso.get().load(Constants.BASE_URL + it.image).placeholder(R.drawable.ic_app)
                    .resize(200, 200).into(holder.imvAvatar)
            } else {
                holder.imvAvatar.setImageResource(R.drawable.ic_app)
            }
            holder.tvName.text = it.name
            holder.tvEmail.text = it.email
            if (isSelected) {
                holder.imvDelete.visibility = View.GONE
                holder.cbSelect.isChecked = selectedIds.contains(it.id)
                holder.cbSelect.setOnCheckedChangeListener { _, b ->
                    if (b) {
                        selectedUser.add(it)
                        selectedIds.add(it.id)
                    } else {
                        selectedUser.removeAt(selectedIds.indexOf(it.id))
                        selectedIds.remove(it.id)
                    }
                }
                holder.itemView.setOnClickListener {
                    holder.cbSelect.isChecked = !holder.cbSelect.isChecked
                }
            } else {
                holder.cbSelect.visibility = View.GONE
                holder.imvDelete.setOnClickListener { _ ->
                    mUsers.remove(it)
                    notifyItemRemoved(holder.adapterPosition)
                }
            }
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal val imvAvatar: ImageView = view.findViewById(R.id.imvAvatar)
        internal val tvName: TextView = view.findViewById(R.id.tvName)
        internal val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        internal val cbSelect: CheckBox = view.findViewById(R.id.cbSelect)
        internal val imvDelete: ImageView = view.findViewById(R.id.imvDelete)
    }
}