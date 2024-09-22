package com.example.firebaseapp.fragments

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.firebaseapp.Model.Users
import com.example.firebaseapp.R
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

class ProfileFragment : Fragment() {

    private lateinit var username: TextView
    private lateinit var imageView: ImageView

    private lateinit var storageReference: StorageReference
    private val IMAGE_REQUEST = 1
    private lateinit var imageUri: Uri
    private var uploadTask: StorageTask<UploadTask.TaskSnapshot>?= null
    private var fuser: FirebaseUser? = null
    private var ref: DatabaseReference? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile,container,false)

        username = view.findViewById(R.id.usernamey)
        imageView = view.findViewById(R.id.profile_image2)

        fuser = FirebaseAuth.getInstance().currentUser
        ref = fuser?.let { FirebaseDatabase.getInstance().getReference("MyUsers").child(it.uid) }

        storageReference = FirebaseStorage.getInstance().getReference("uploads")

        ref?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                username.setText(user?.username)
                if (user!=null && user.imageURL == "default"){
                    imageView.setImageResource(R.mipmap.ic_launcher)
                }else{
                    context?.let { Glide.with(it).load(user?.imageURL).into(imageView) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        imageView.setOnClickListener(){
            SelectImage()
        }

        return view

    }

    private fun SelectImage(){
        val i = Intent()
        i.setType("image/*")
        i.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(i,IMAGE_REQUEST)
    }

    private fun getFileExtension(uri: Uri):String?{
        val contentResolver = context?.contentResolver
        val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver?.getType(uri))
    }

    private fun uploadImage(){
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Uploading")
        progressDialog.show()

        if (imageUri!=null){
            val fileReference: StorageReference = storageReference.child("${System.currentTimeMillis()}.${getFileExtension(imageUri)}")

            uploadTask = fileReference.putFile(imageUri)
            (uploadTask as UploadTask).continueWithTask{ task->
                if (!task.isSuccessful){
                    task.exception?.let{
                        throw it
                    }
                }
                fileReference.downloadUrl
            }.addOnCompleteListener(){
                if (it.isSuccessful){
                    val downloadUri = it.getResult()
                    val mUri = downloadUri.toString()
                    ref = fuser?.uid?.let { it1 ->
                        FirebaseDatabase.getInstance().getReference("MyUsers").child(it1)
                    }
                    val map = HashMap<String,Any>()
                    map.put("imageURL",mUri)
                    ref?.updateChildren(map)

                    progressDialog.dismiss()
                }else{
                    Toast.makeText(context,"Failure!!",Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener(){
                Toast.makeText(context,it.message,Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }else{
            Toast.makeText(context,"No Image Selected",Toast.LENGTH_SHORT).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==IMAGE_REQUEST && resultCode== RESULT_OK && data !=null && data.data!=null){
            imageUri = data.data!!

            if (uploadTask!=null && uploadTask!!.isInProgress){
                Toast.makeText(context,"Upload in Progress",Toast.LENGTH_SHORT).show()
            }else{
                uploadImage()
            }
        }
    }

}