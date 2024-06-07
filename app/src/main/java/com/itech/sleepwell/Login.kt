package com.itech.sleepwell

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val login : AppCompatButton = findViewById(R.id.login_bttn)
        var signup = findViewById<TextView>(R.id.sign_up_bttn)
        var forgot_pass = findViewById<TextView>(R.id.forgot_pass)

        signup.setOnClickListener {
            val intent = Intent (this, Signup::class.java)
            startActivity(intent)
        }

        forgot_pass.setOnClickListener {
            val intent = Intent (this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }
}