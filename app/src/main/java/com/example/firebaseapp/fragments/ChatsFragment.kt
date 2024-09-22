package com.example.firebaseapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseapp.Adapter.UserAdapter
import com.example.firebaseapp.Model.ChatList
import com.example.firebaseapp.Model.Users
import com.example.firebaseapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChatsFragment : Fragment() {

    private lateinit var userAdapter: UserAdapter
    private lateinit var mUsers:MutableList<Users>
    private var fuser: FirebaseUser? = null
    private var ref: DatabaseReference? = null
    private lateinit var usersList: MutableList<ChatList>
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats,container,false)

        recyclerView = view.findViewById(R.id.recycler_view2)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        fuser = FirebaseAuth.getInstance().currentUser

        ref = fuser?.let { FirebaseDatabase.getInstance().getReference("ChatList").child(it.uid) }
        ref?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()

                for (dataSnapshot in snapshot.children){
                    val chatlist = dataSnapshot.getValue(ChatList::class.java)

                    if (chatlist!=null){
                        usersList.add(chatlist)
                    }
                }
                chatList()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        return view
    }

    private fun chatList(){
        ref = FirebaseDatabase.getInstance().getReference("MyUsers")
        ref?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                mUsers.clear()
                for (dataSnapshot in snapshot.children){
                    val user = dataSnapshot.getValue(Users::class.java)

                    for (chatList in usersList){
                        if (user!=null && user.id==chatList.id){
                            mUsers.add(user)
                        }
                    }
                }
                userAdapter = UserAdapter(context!!,mUsers,true)
                recyclerView.adapter = userAdapter

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}