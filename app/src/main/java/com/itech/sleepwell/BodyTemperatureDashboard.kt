package com.itech.sleepwell

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BodyTemperatureDashboard : AppCompatActivity() {

    private val splash_time: Long = 10000
    private lateinit var bodyTempValueTextView: TextView
    private lateinit var bodyTempTimestampTextView: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_body_temperature_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        bodyTempValueTextView = findViewById(R.id.body_temp_value)
        bodyTempTimestampTextView = findViewById(R.id.body_temp_timestamp)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            // Initialize Firebase Realtime Database with current user's ID
            database = FirebaseDatabase.getInstance().getReference("SleepwellDevice/Sensor/$userId")
            setupFirebaseListener()
        } else {
            // Handle case where user is not signed in
            bodyTempValueTextView.text = "No User"
            bodyTempTimestampTextView.text = "N/A"
        }

        setupUI()
    }

    private fun setupFirebaseListener() {
        database.child("temperature").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Update the temperature value
                    val temperature = snapshot.getValue(Double::class.java)
                    bodyTempValueTextView.text = String.format("%.1f°C", temperature)

                    // Update the timestamp with the current time
                    val currentTime = System.currentTimeMillis()
                    val timestamp = android.text.format.DateFormat.format("HH:mm:ss", currentTime)
                    bodyTempTimestampTextView.text = timestamp.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
                bodyTempValueTextView.text = "Error"
                bodyTempTimestampTextView.text = "N/A"
            }
        })
    }

    private fun setupUI() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                startActivity(Intent(applicationContext, HomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.connect) {
                startActivity(Intent(applicationContext, BluetoothDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.profile) {
                startActivity(Intent(applicationContext, ProfileDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.updates) {
                startActivity(Intent(applicationContext, UpdatesDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.logout) {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                return@setOnItemSelectedListener true
            }
            false
        }

        // Find the ImageView and load the GIF file
        val imageView = findViewById<ImageView>(R.id.img_logo)
        Glide.with(this).load(R.drawable.nick_pillow).into(imageView)

        Handler().postDelayed({
        }, splash_time)
    }
}