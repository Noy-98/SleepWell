package com.itech.sleepwell

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.UUID

class HomeDashboard : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = Firebase.database

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.connect) {
                connectDevice()
                true
            } else if (item.itemId == R.id.profile) {
                startActivity(Intent(applicationContext, ProfileDashboard::class.java))
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

        val Heart_Monitoring = findViewById<CardView>(R.id.heart_monitoring)
        val Sweat_Monitoring = findViewById<CardView>(R.id.sweat_monitoring)
        val Body_Temp_Monitoring = findViewById<CardView>(R.id.body_temp_monitoring)
        val Stress_Level_Monitoring = findViewById<CardView>(R.id.stress_level_monitoring)

        Heart_Monitoring.setOnClickListener {
            val intent = Intent (this, HeartDashboard::class.java)
            startActivity(intent)
        }
        Sweat_Monitoring.setOnClickListener {
            val intent = Intent(this, SweatDashboard::class.java)
            startActivity(intent)
        }
        Body_Temp_Monitoring.setOnClickListener {
            val intent = Intent(this, BodyTemperatureDashboard::class.java)
            startActivity(intent)
        }
        Stress_Level_Monitoring.setOnClickListener {
            val intent = Intent(this, StressLevelDashboard::class.java)
            startActivity(intent)
        }
    }

    private fun connectDevice() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            val deviceRef = database.getReference("SleepWellDevice")
            deviceRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(this@HomeDashboard, "You are already connected to your device", Toast.LENGTH_SHORT).show()
                    } else {
                        val deviceId = deviceRef.push().key ?: return
                        val deviceData = mapOf(
                            "id" to deviceId,
                            "uid" to uid,
                            "heartRateData" to "",
                            "sweatLevelData" to "",
                            "bodyTempData" to "",
                            "timestamp" to System.currentTimeMillis()
                        )

                        deviceRef.child(deviceId).setValue(deviceData).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@HomeDashboard, "Connected Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@HomeDashboard, "Failed to connect device: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@HomeDashboard, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}