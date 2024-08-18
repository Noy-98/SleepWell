package com.itech.sleepwell

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MusicDashboard : AppCompatActivity() {

    private lateinit var musicList: RecyclerView
    private lateinit var adapter: MusicAdapter
    private lateinit var searchBox: EditText
    private val allMusicList = mutableListOf<Music>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check Bluetooth status
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            showBluetoothAlertDialog()
        } else {
            setupUI()
        }
    }

    private fun showBluetoothAlertDialog() {
        AlertDialog.Builder(this)
            .setTitle("Bluetooth Required")
            .setMessage("Bluetooth is not enabled. Please enable Bluetooth to use the Music Dashboard.")
            .setPositiveButton("OK") { _, _ ->
                startActivity(Intent(this, HomeDashboard::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun setupUI() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                if (::adapter.isInitialized) {
                    adapter.pauseMusicOnDashboard()
                }

                startActivity(Intent(applicationContext, HomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.connect) {
                if (::adapter.isInitialized) {
                    adapter.pauseMusicOnDashboard()
                }

                startActivity(Intent(applicationContext, BluetoothDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.profile) {
                if (::adapter.isInitialized) {
                    adapter.pauseMusicOnDashboard()
                }

                startActivity(Intent(applicationContext, ProfileDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.updates) {
                if (::adapter.isInitialized) {
                    adapter.pauseMusicOnDashboard()
                }

                startActivity(Intent(applicationContext, UpdatesDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.logout) {
                if (::adapter.isInitialized) {
                    adapter.stopMusicOnLogout() // Stop the music before logging out
                }

                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, Login::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return@setOnItemSelectedListener true
            }
            false
        }

        musicList = findViewById(R.id.musicList)
        musicList.layoutManager = LinearLayoutManager(this)

        searchBox = findViewById(R.id.search_box)

        // Request permission and fetch music files
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            fetchMusicFiles()
        }

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMusicList(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterMusicList(query: String) {
        val filteredList = allMusicList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true)
        }
        adapter.updateMusicList(filteredList)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchMusicFiles()
        } else {
            Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMusicFiles() {
        val contentResolver = contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA
        )
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            while (it.moveToNext()) {
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val filePath = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                allMusicList.add(Music(title, artist, filePath))
            }
        }

        adapter = MusicAdapter(this, allMusicList)
        musicList.adapter = adapter
    }
}
