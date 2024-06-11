package com.itech.sleepwell

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class ProfileDashboard : AppCompatActivity() {

    private lateinit var imageUri: Uri
    private lateinit var storageReference: StorageReference
    private lateinit var databaseReference: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    imageUri = uri
                    val profileImageView = findViewById<ShapeableImageView>(R.id.profile_pic)
                    Glide.with(this)
                        .load(imageUri)
                        .into(profileImageView)
                }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storageReference = FirebaseStorage.getInstance().reference
        databaseReference = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.profile
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                startActivity(Intent(applicationContext, HomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.connect) {
                connectDevice()
                true
            } else if (item.itemId == R.id.profile) {
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
        val editProfileButton = findViewById<AppCompatButton>(R.id.change_profile_bttn)
        editProfileButton.setOnClickListener {
            updateProfile()
        }

        val editImageButton = findViewById<FloatingActionButton>(R.id.upload_bttn)
        editImageButton.setOnClickListener {
            openImagePicker()
        }

        loadUsersProfile()
    }

    private fun connectDevice() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            val deviceRef = databaseReference.getReference("SleepWellDevice")
            deviceRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Toast.makeText(this@ProfileDashboard, "You are already connected to your device", Toast.LENGTH_SHORT).show()
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
                                Toast.makeText(this@ProfileDashboard, "Connected Successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ProfileDashboard, "Failed to connect device: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@ProfileDashboard, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val usersReference = databaseReference.getReference("Users/$uid")

            // Update profile picture
            if (::imageUri.isInitialized) {
                val imageRef = storageReference.child("images/$uid/${imageUri.lastPathSegment}")
                imageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                        imageRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                            usersReference.child("imageUrl").setValue(uri.toString())
                        }
                    }
            }

            // Update other profile information
            val firstName = findViewById<TextInputEditText>(R.id.first_name).text.toString()
            val lastName = findViewById<TextInputEditText>(R.id.last_name).text.toString()
            val mobileNum = findViewById<TextInputEditText>(R.id.mobile_number).text.toString()

            usersReference.child("firstname").setValue(firstName)
            usersReference.child("lastname").setValue(lastName)
            usersReference.child("mobilenum").setValue(mobileNum)

            // Update password
            val password = findViewById<TextInputEditText>(R.id.password).text.toString()
            val confirmPassword = findViewById<TextInputEditText>(R.id.confirm_password).text.toString()

            if (password.isNotEmpty() && password.length >= 6) {
                if (password == confirmPassword) {
                    currentUser.updatePassword(password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else if (password.isNotEmpty()) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }

    private fun loadUsersProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null){
            val uid = currentUser.uid
            val usersReference = FirebaseDatabase.getInstance().getReference("Users/$uid")

            usersReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val users = snapshot.getValue(Users::class.java)
                        if (users != null) {
                            val profileImageView =
                                findViewById<ShapeableImageView>(R.id.profile_pic)
                            Glide.with(this@ProfileDashboard)
                                .load(users.imageUrl)
                                .into(profileImageView)

                            // Set the text views with the doctor's information
                            findViewById<TextView>(R.id.first_name).text = users.firstname
                            findViewById<TextView>(R.id.last_name).text = users.lastname
                            findViewById<TextView>(R.id.mobile_number).text = users.mobilenum
                            findViewById<TextView>(R.id.email).text = users.email
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ProfileDashboard, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }
}