package com.example.firebaseapp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebaseapp.MessageActivity
import com.example.firebaseapp.Model.Chat
import com.example.firebaseapp.Model.Users
import com.example.firebaseapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MessageAdapter(private val context: Context,private val mChat: List<Chat>, private val imgURL: String):
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private var fuser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == MSG_TYPE_RIGHT){
            val view: View = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false)
            return ViewHolder(view)
        }else{
            val view: View = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false)
            return ViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = mChat[position]
        holder.show_message.setText(chat.message)
        if (imgURL == "default"){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher)
        }else{
            Glide.with(context).load(imgURL).into(holder.profile_image)
        }

        if (position ==mChat.size -1){
            if (chat.isseen){
                holder.text_seen.setText("seen")
            }else{
                holder.text_seen.setText("delivered")
            }
        }else{
            holder.text_seen.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mChat.size
    }

    class ViewHolder(val itemView:View):RecyclerView.ViewHolder(itemView){
        val show_message: TextView = itemView.findViewById(R.id.show_message)
        val profile_image: ImageView = itemView.findViewById(R.id.profile_image)
        val text_seen: TextView = itemView.findViewById(R.id.text_seen_status)
    }

    override fun getItemViewType(position: Int): Int {
        fuser?.let { user ->
            return if (mChat[position].sender == user.uid) {
                MSG_TYPE_RIGHT
            } else {
                MSG_TYPE_LEFT
            }
        }
        return MSG_TYPE_LEFT
    }
}