package com.example.firebaseapp

import android.content.Intent
import  androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var userET:EditText
    private lateinit var passET:EditText
    private lateinit var emailET:EditText
    private lateinit var registerBtn:Button
    private lateinit var auth:FirebaseAuth
    private lateinit var myRef:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userET = findViewById(R.id.userEditText)
        passET = findViewById(R.id.passEditText2)
        emailET = findViewById(R.id.emailEditText)
        registerBtn = findViewById(R.id.buttonRegister)

        auth = FirebaseAuth.getInstance()

        registerBtn.setOnClickListener {
            val username_text = userET.text.toString()
            val email_text = emailET.text.toString()
            val pass_text = passET.text.toString()

            if (TextUtils.isEmpty(username_text) || TextUtils.isEmpty(email_text) || TextUtils.isEmpty(pass_text)){
                Toast.makeText(this,"Please Fill All Fields",Toast.LENGTH_SHORT).show()
            }else{
                RegisterNow(username_text,email_text,pass_text)
            }
        }
    }

    private fun RegisterNow(username:String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(){
            if (it.isSuccessful()){
                val firebaseUser: FirebaseUser? = auth.currentUser
                val userId: String? = firebaseUser?.uid
                myRef = userId?.let { it1 ->
                    FirebaseDatabase.getInstance().getReference("MyUsers").child(it1)
                }!! //not null
                val hashMap = mutableMapOf<String,String>()
                hashMap.put("id", userId)
                hashMap.put("username", username)
                hashMap.put("imageURL", "default")  //default url
                hashMap.put("status","offline")

                myRef.setValue(hashMap).addOnCompleteListener(){
                    if (it.isSuccessful()){
                        var i: Intent = Intent(this,MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                        finish()
                    }
                }
            }else{
                val errorMessage = it.exception?.message
                Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }
    }
}