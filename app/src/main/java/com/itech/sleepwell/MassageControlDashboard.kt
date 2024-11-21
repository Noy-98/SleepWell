package com.itech.sleepwell


import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MassageControlDashboard : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_massage_control_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            // Initialize Firebase Database reference
            database = FirebaseDatabase.getInstance().getReference("SleepwellDevice/Sensor/$userId")
        } else {
            Toast.makeText(this, "No user signed in.", Toast.LENGTH_SHORT).show()
            return
        }

        setupUI()
        setupSwitchListener()
    }

    private fun setupSwitchListener() {
        val switchControl = findViewById<Switch>(R.id.switch_control)

        // Set listener for the switch
        switchControl.setOnCheckedChangeListener { _, isChecked ->
            val value = if (isChecked) 1 else 0
            database.child("channelRelayState1").setValue(value).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        if (isChecked) "Switched ON" else "Switched OFF",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Failed to update state", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    }
}