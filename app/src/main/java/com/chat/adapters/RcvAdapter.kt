//package com.base.adapters
//
//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.base.R
//
//class RcvAdapter (private val context: Context, private val mTours: ArrayList<String>): RecyclerView.Adapter<RcvAdapter.ViewHolder>() {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rcv_tour_list, parent, false))
//    }
//
//    override fun getItemCount(): Int {
//        return mTours.size
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        mTours[position].let {
//            holder.tvName.text = it.tourName
//        }
//    }
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        internal val tvName: TextView = view.findViewById(R.id.tvName)
//    }
//}