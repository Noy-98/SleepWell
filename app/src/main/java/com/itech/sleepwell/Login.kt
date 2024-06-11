package com.itech.sleepwell

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        val Email : TextInputEditText = findViewById(R.id.email)
        val Password : TextInputEditText = findViewById(R.id.pass)
        val ProgressBar : ProgressBar = findViewById(R.id.signInProgressBar)
        val login : AppCompatButton = findViewById(R.id.login_bttn)
        var signup = findViewById<TextView>(R.id.sign_up_bttn)
        var forgot_pass = findViewById<TextView>(R.id.forgot_pass)

        login.setOnClickListener {
            ProgressBar.visibility = View.VISIBLE

            val email = Email.text.toString()
            val password = Password.text.toString()

            if (email.isEmpty() || password.isEmpty()){
                if (email.isEmpty()){
                    Email.error = "Enter email address"
                }
                if (password.isEmpty()){
                    Password.error = "Enter your password"
                }
                Toast.makeText(this,"All fields are Required!", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            } else if (!email.matches(emailPattern.toRegex())){
                ProgressBar.visibility = View.GONE
                Email.error="Enter valid email address"
                Toast.makeText(this,"Enter valid email address", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6){
                ProgressBar.visibility = View.GONE
                Password.error="Wrong Password"
                Toast.makeText(this,"Enter your password more than 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {loginTask ->
                    if (loginTask.isSuccessful){
                        val user = auth.currentUser
                        if (user != null) {
                            if(user.isEmailVerified){
                                ProgressBar.visibility = View.GONE
                                val intent = Intent(this, HomeDashboard::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                Toast.makeText(this, "Login Successfully", Toast.LENGTH_SHORT).show()
                            }else {
                                ProgressBar.visibility = View.GONE
                                Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        ProgressBar.visibility = View.GONE
                        Toast.makeText(this, "Something went wrong, try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

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