package com.example.reycerio.kotlinmessenger.registration

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.reycerio.kotlinmessenger.messages.LatestMessagesActivity
import com.example.reycerio.kotlinmessenger.R
import com.example.reycerio.kotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var selectedPhotoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {
            performRegister()
        }

        already_have_an_account_text_view_register.setOnClickListener{
            Log.d("MainActivity", "try to show login activity")
//            launch the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        select_photo_button_register.setOnClickListener {
            selectPhoto()
        }
    }

    //need to be called for the image picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null ) {
            //proceed to see what image was...
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val bitmapDrawable = BitmapDrawable(bitmap)
            select_photo_button_register.setBackgroundDrawable(bitmapDrawable)
            select_photo_button_register.text = ""
        }
    }

    //return@addOnCompleteListener is for override functions

    private fun performRegister() {
        val email = email_editText_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter a valid email and password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MainActivity", "Email is:" + email)
        Log.d("MainActivity", "password is: $password")

        //login to firebase with email and pw
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    uploadImageToFirebaseStorage()
                    //else
                    Log.d("Main", "user id is : ${it.result.user.uid}")
                    Toast.makeText(this, "user id is : ${it.result.user.uid}", Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener {
                    Log.d("Main", "Failed to register user: ${it.message}")
                    Toast.makeText(this, "Failed to register user: ${it.message}", Toast.LENGTH_SHORT).show()

                }
    }

    private fun selectPhoto() {
        Log.d("Main", "select photo button pressed")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("Main", "Successfully uploaded image to firebase storage: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("Main", "$it")
                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener {
                    //always add a failure listener too to catch and handle errors
                }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl : String) {
        val uid = FirebaseAuth.getInstance().uid.toString()  // or you can use an elvis operator and ?: default it to empty string (?: "")
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, userName_editText_registrer.text.toString(), profileImageUrl)

        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("Main", "Successfully saved user to database.")
                    val intent =  Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)  //clears your activity stack
                    startActivity(intent)
                }
                .addOnFailureListener {
                    //always add a failure listener too to catch and handle errors
                }
    }
}

//class User(val uid: String, val username: String, val profileImageUrl: String): Parcelable {
//    //need the constructor if youre gonna plug in data into empty class
//    constructor() : this("", "", "")
//}
