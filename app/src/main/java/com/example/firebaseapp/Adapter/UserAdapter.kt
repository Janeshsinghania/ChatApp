package com.example.firebaseapp.Adapter

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebaseapp.MessageActivity
import com.example.firebaseapp.Model.Users
import com.example.firebaseapp.R

class UserAdapter(private val context: Context,private val mUsers: List<Users>, private val isChat: Boolean):
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.user_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val users = mUsers[position]
        holder.username.text = users.username

        if (users.imageURL=="default"){
            holder.imageView.setImageResource(R.mipmap.ic_launcher)
        }else{
            Glide.with(context).load(users.imageURL).into(holder.imageView)
        }
        //status check
        if (isChat){
            if (users.status == "online"){
                holder.imageViewON.visibility = View.VISIBLE
                holder.imageViewOFF.visibility = View.GONE
            }else{
                holder.imageViewOFF.visibility = View.VISIBLE
                holder.imageViewON.visibility = View.GONE
            }
        }else{
            holder.imageViewON.visibility = View.GONE
            holder.imageViewOFF.visibility = View.GONE
        }

        holder.itemView.setOnClickListener(){
            var i: Intent = Intent(context, MessageActivity::class.java)
            i.putExtra("userid",users.id)
            context.startActivity(i)
        }
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    class ViewHolder(val itemView:View):RecyclerView.ViewHolder(itemView){
        val username: TextView = itemView.findViewById(R.id.textView30)
        val imageView: ImageView = itemView.findViewById(R.id.imageView30)
        val imageViewON: ImageView = itemView.findViewById(R.id.statusimageON)
        val imageViewOFF: ImageView = itemView.findViewById(R.id.statusimageOFF)
    }
}