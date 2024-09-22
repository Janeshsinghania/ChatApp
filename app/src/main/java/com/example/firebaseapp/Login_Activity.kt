package com.example.firebaseapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Login_Activity : AppCompatActivity() {

    private lateinit var userETLogin: EditText
    private lateinit var passETLogin: EditText
    private lateinit var loginBtn: Button
    private lateinit var RegisterBtn: Button
    private lateinit var auth: FirebaseAuth
    private var firebaseUser: FirebaseUser? = null

    override fun onStart() {
        super.onStart()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        //checking for existing user
        if (firebaseUser != null){
            var i = Intent(this,MainActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userETLogin = findViewById(R.id.editText)
        passETLogin = findViewById(R.id.editText3)
        loginBtn = findViewById(R.id.buttonLogin)
        RegisterBtn = findViewById(R.id.registerBtn)

        auth = FirebaseAuth.getInstance()

        RegisterBtn.setOnClickListener {
            var i = Intent(this,RegisterActivity::class.java)
            startActivity(i)
        }

        loginBtn.setOnClickListener {
            var email_text = userETLogin.text.toString()
            var pass_text = passETLogin.text.toString()

            if (TextUtils.isEmpty(email_text) or TextUtils.isEmpty(pass_text)){
                Toast.makeText(this,"Please fill the Fields",Toast.LENGTH_SHORT).show()
            }else{
                auth.signInWithEmailAndPassword(email_text,pass_text).addOnCompleteListener(){
                    if (it.isSuccessful){
                        var i: Intent = Intent(this,MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                        finish()
                    }else{
                        Toast.makeText(this,"Login Failed!",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}