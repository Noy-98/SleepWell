package com.itech.sleepwell

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class HeartDashboard : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var heartRateValueTextView: TextView
    private lateinit var heartRateTimestampTextView: TextView
    private lateinit var deviceRef: Query
    private var valueEventListener: ValueEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_heart_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize UI elements
        heartRateValueTextView = findViewById(R.id.heart_rate_value)
        heartRateTimestampTextView = findViewById(R.id.heart_rate_timestamp)

        // Set up database reference and listener
        setupDatabaseListener()
    }

    private fun setupDatabaseListener() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            deviceRef = database.getReference("SleepWellDevice").orderByChild("uid").equalTo(uid)

            valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (deviceSnapshot in dataSnapshot.children) {
                            val heartRateData = deviceSnapshot.child("heartRateData").getValue(String::class.java)?.toIntOrNull()
                            val timestamp = deviceSnapshot.child("timestamp").getValue(Long::class.java)

                            heartRateValueTextView.text = (heartRateData ?: "0").toString()
                            heartRateTimestampTextView.text = timestamp?.let { convertTimestampToDate(it) } ?: "N/A"
                        }
                    } else {
                        Toast.makeText(this@HeartDashboard, "No device data found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@HeartDashboard, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            }

            deviceRef.addValueEventListener(valueEventListener!!)
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove the listener to avoid memory leaks
        if (valueEventListener != null) {
            deviceRef.removeEventListener(valueEventListener!!)
        }
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        val date = java.util.Date(timestamp)
        return sdf.format(date)
    }
}