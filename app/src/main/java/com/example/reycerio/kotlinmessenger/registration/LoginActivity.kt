package com.example.reycerio.kotlinmessenger.registration

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.reycerio.kotlinmessenger.R
import com.example.reycerio.kotlinmessenger.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener{
            performLogIn()
        }

        back_to_register_text_view_login.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogIn() {
        val email = email_edit_text_login.text.toString()
        val password = password_edit_text_login.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
        }

        Log.d("LoginActivity", "email is: $email and password is: $password")
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d("Main", "User Logged in with user id: ${it.user.uid}")
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Log.d("Main", "Failed to log in user: ${it.message}")
                }
    }
}