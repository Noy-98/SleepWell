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

class BodyTemperatureDashboard : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var bodyTempValueTextView: TextView
    private lateinit var bodyTempTimestampTextView: TextView
    private lateinit var deviceRef: Query
    private var valueEventListener: ValueEventListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_body_temperature_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                startActivity(Intent(applicationContext, Login::class.java))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            }
            false
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Initialize UI elements
        bodyTempValueTextView = findViewById(R.id.body_temp_value)
        bodyTempTimestampTextView = findViewById(R.id.body_temp_timestamp)

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
                            val bodyTempData = deviceSnapshot.child("bodyTempData").getValue(String::class.java)?.toIntOrNull()
                            val timestamp = deviceSnapshot.child("timestamp").getValue(Long::class.java)

                            bodyTempValueTextView.text = (bodyTempData ?: "0").toString()
                            bodyTempTimestampTextView.text = timestamp?.let { convertTimestampToDate(it) } ?: "N/A"
                        }
                    } else {
                        Toast.makeText(this@BodyTemperatureDashboard, "No device data found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@BodyTemperatureDashboard, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
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