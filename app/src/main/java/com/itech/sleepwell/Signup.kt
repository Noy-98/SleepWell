package com.itech.sleepwell

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val firstname = findViewById<TextInputEditText>(R.id.first_name)
        val lastname = findViewById<TextInputEditText>(R.id.last_name)
        val mobile_num = findViewById<TextInputEditText>(R.id.mobile_number)
        val email = findViewById<TextInputEditText>(R.id.email)
        val password = findViewById<TextInputEditText>(R.id.password)
        val confirm_pass = findViewById<TextInputEditText>(R.id.confirm_password)
        val ProgressBar : ProgressBar = findViewById(R.id.signUpProgressBar)
        val signup = findViewById<AppCompatButton>(R.id.signup_bttn)
        val login = findViewById<TextView>(R.id.login_bttn)

        signup.setOnClickListener {
            val fname = firstname.text.toString()
            val lname = lastname.text.toString()
            val mobNum = mobile_num.text.toString()
            val em = email.text.toString()
            val pass = password.text.toString()
            val cpass = confirm_pass.text.toString()

            ProgressBar.visibility = View.VISIBLE


            if (fname.isEmpty() || lname.isEmpty() || mobNum.isEmpty() || em.isEmpty() || pass.isEmpty() || cpass.isEmpty()){
                if (fname.isEmpty()){
                    firstname.error = "Enter your firstname"
                }
                if (lname.isEmpty()){
                    lastname.error = "Enter your lastname"
                }
                if (mobNum.isEmpty()){
                    mobile_num.error = "Enter your mobile number"
                }
                if (em.isEmpty()){
                    email.error = "Enter your email"
                }
                if (pass.isEmpty()){
                    password.error = "Enter your password"
                }
                if (cpass.isEmpty()){
                    confirm_pass.error = "Enter your password"
                }
                Toast.makeText(this,"All fields are Required!", Toast.LENGTH_SHORT).show()
                ProgressBar.visibility = View.GONE
            } else if (!em.matches(emailPattern.toRegex())){
                ProgressBar.visibility = View.GONE
                email.error="Enter valid email address"
                Toast.makeText(this,"Enter valid email address", Toast.LENGTH_SHORT).show()
            } else if (pass.length < 6){
                ProgressBar.visibility = View.GONE
                password.error="Enter your password more than 6 characters"
                Toast.makeText(this,"Enter your password more than 6 characters", Toast.LENGTH_SHORT).show()
            } else if (pass != cpass){
                ProgressBar.visibility = View.GONE
                confirm_pass.error="Password not match"
                Toast.makeText(this,"Password not match", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(em, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnSuccessListener {
                                Toast.makeText(this, "Verification email sent. Please check your email to verify your account.", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to send verification email: ${e.message}", Toast.LENGTH_SHORT).show()
                            }

                        val databaseRef = database.reference.child("Users").child(auth.currentUser!!.uid)
                        val users: Users = Users(em, fname, lname, mobNum, pass, auth.currentUser!!.uid)

                        databaseRef.setValue(users).addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                ProgressBar.visibility = View.GONE
                                val intent = Intent(this, Login::class.java)
                                startActivity(intent)
                                Toast.makeText(this, "Sign Up Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                ProgressBar.visibility = View.GONE
                                Log.d("Firebase", "Database write failed: ${dbTask.exception}")
                                Toast.makeText(this, "Sign Up Failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        ProgressBar.visibility = View.GONE
                        Log.d("Firebase", "User creation failed: ${task.exception}")
                        Toast.makeText(this, "Do you have internet Connection? or Do you have already account?, try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        login.setOnClickListener {
            val intent = Intent (this, Login::class.java)
            startActivity(intent)
        }
    }
}