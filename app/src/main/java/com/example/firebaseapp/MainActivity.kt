package com.example.firebaseapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.firebaseapp.Model.Users
import com.example.firebaseapp.fragments.ChatsFragment
import com.example.firebaseapp.fragments.ProfileFragment
import com.example.firebaseapp.fragments.UsersFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private var firebaseUser: FirebaseUser? = null
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.let {
            myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(it.uid)
        } ?: kotlin.run {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users: Users? = snapshot.getValue(Users::class.java)
                if (users != null) {
                    Toast.makeText(
                        this@MainActivity,
                        "User Login: ${users.username}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        val tabLayout: TabLayout = findViewById(R.id.tabLayout2)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val fragments = mutableListOf<Fragment>()
        val titles = mutableListOf<String>()

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, fragments, titles)
        viewPagerAdapter.addFragment(ChatsFragment(), "Chats")
        viewPagerAdapter.addFragment(UsersFragment(), "Users")
        viewPagerAdapter.addFragment(ProfileFragment(), "Profile")

        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

    }

    //Adding logout functionality
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, Login_Activity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                return true
            }

            else -> return false
        }
    }

    class ViewPagerAdapter(
        fm: FragmentManager,
        private val fragments: MutableList<Fragment>,
        private val titles: MutableList<String>
    ) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }


    }

    fun checkStatus(status: String){
        if (firebaseUser != null) {
            myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser!!.uid)
        }
        val hashMap = hashMapOf<String,Any>()
        hashMap.put("status",status)
        myRef.updateChildren(hashMap)
    }

    override fun onResume() {
        super.onResume()
        checkStatus("online")
    }

    override fun onPause() {
        super.onPause()
        checkStatus("offline")
    }

}