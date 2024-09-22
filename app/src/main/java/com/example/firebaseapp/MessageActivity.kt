package com.example.firebaseapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebaseapp.Adapter.MessageAdapter
import com.example.firebaseapp.Model.Chat
import com.example.firebaseapp.Model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class MessageActivity : AppCompatActivity() {

    private lateinit var username: TextView
    private lateinit var imageView: ImageView

    private lateinit var recyclerViewy: RecyclerView
    private lateinit var msg_editText: EditText
    private lateinit var sendBtn: ImageButton
    private lateinit var userid:String

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var seenListener: ValueEventListener

    private var fuser: FirebaseUser? = null
    private lateinit var reference: DatabaseReference
    private lateinit var intent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        username = findViewById(R.id.username)
        imageView = findViewById(R.id.imageview_profile)
        sendBtn = findViewById(R.id.btn_send)
        msg_editText = findViewById(R.id.text_send)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

//        val toolbar: Toolbar = findViewById(R.id.toolbar2)
//        setSupportActionBar(toolbar)
//        supportActionBar?.title = ""
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//        toolbar.setNavigationOnClickListener(){
//            finish()
//        }
        intent = getIntent()
        userid = intent.getStringExtra("userid")?: ""

        fuser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.getValue(Users::class.java)
                if (users != null) {
                    username.text = users.username

                    if (users.imageURL.equals("default")) {
                        imageView.setImageResource(R.mipmap.ic_launcher)
                    } else {
                        Glide.with(this@MessageActivity).load(users.imageURL).into(imageView)
                    }
                    fuser?.let { readMessage(it.uid, userid, users.imageURL) }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        sendBtn.setOnClickListener() {
            val msg = msg_editText.text.toString()
            if (!msg.equals("")) {
                fuser?.uid?.let { it1 ->
                    sendMessage(it1, userid, msg)
                }
            } else {
                Toast.makeText(this, "Please send a non empty message", Toast.LENGTH_SHORT).show()
            }
            msg_editText.setText("")
        }
        seenMessage(userid)
    }

    private fun seenMessage(userid: String){
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        seenListener = reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!=null && chat.receiver == fuser?.uid && chat.sender == userid){
                        val hashmap = HashMap<String,Any>()
                        hashmap.put("isseen",true)
                        dataSnapshot.ref.updateChildren(hashmap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun sendMessage(sender: String, receiver: String, message: String) {
        val reference = FirebaseDatabase.getInstance().getReference()
        val hashMap = mutableMapOf<String, Any>()
        hashMap.put("sender", sender)
        hashMap.put("receiver", receiver)
        hashMap.put("message", message)
        hashMap.put("isseen",false)

        reference.child("Chats").push().setValue(hashMap)

        //adding Users to chat fragment
        val chatReference: DatabaseReference? =
            fuser?.let {
                FirebaseDatabase.getInstance().getReference("ChatList").child(it.uid).child(userid)
            }
        if (chatReference != null) {
            chatReference.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()){
                        chatReference.child("id").setValue(userid)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }

    private fun readMessage(myid: String, userid: String, imageURL: String) {
        val chatlist = ArrayList<Chat>()
        reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatlist.clear()
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat != null) {
                        if (chat.receiver == myid && chat.sender == userid || chat.receiver == userid && chat.sender == myid) {
                            chatlist.add(chat)
                        }
                    }
                }
                messageAdapter = MessageAdapter(this@MessageActivity, chatlist, imageURL)
                recyclerView.adapter = messageAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    fun checkStatus(status: String){
        if (fuser != null) {
            reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser!!.uid)
        }
        val hashMap = hashMapOf<String,Any>()
        hashMap.put("status",status)
        reference.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        checkStatus("online")
    }

    override fun onPause() {
        super.onPause()
        reference.removeEventListener(seenListener)
        checkStatus("offline")
    }

}