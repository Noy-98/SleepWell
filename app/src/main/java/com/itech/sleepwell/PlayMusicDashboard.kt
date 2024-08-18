package com.itech.sleepwell

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayInputStream
import kotlin.random.Random

class PlayMusicDashboard : AppCompatActivity() {

    private lateinit var musicName: TextView
    private lateinit var artist: TextView
    private lateinit var duration: TextView
    private lateinit var totalDuration: TextView
    private lateinit var prev: ImageView
    private lateinit var playPause: ImageView
    private lateinit var next: ImageView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var musicImage: ImageView
    private lateinit var musicControl: ImageView

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private lateinit var musicList: List<Music>
    private var currentPosition: Int = 0

    private enum class PlaybackMode {
        REPEAT_ALL, REPEAT_ONE, SHUFFLE
    }

    private var playbackMode = PlaybackMode.REPEAT_ALL

    private val handler = Handler(Looper.getMainLooper())
    private val updateDurationRunnable = object : Runnable {
        override fun run() {
            // Update the current duration text
            mediaPlayer?.let {
                val currentPosition = it.currentPosition
                duration.text = formatDuration(currentPosition)
                progressIndicator.progress = (currentPosition * 100) / (it.duration)
            }
            handler.postDelayed(this, 1000) // Update every second
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_play_music_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.selectedItemId = R.id.home
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            if (item.itemId == R.id.home) {
                mediaPlayer?.pause()
                isPlaying = false

                startActivity(Intent(applicationContext, HomeDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.connect) {
                mediaPlayer?.pause()
                isPlaying = false

                startActivity(Intent(applicationContext, BluetoothDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.profile) {
                mediaPlayer?.pause()
                isPlaying = false

                startActivity(Intent(applicationContext, ProfileDashboard::class.java))
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
                return@setOnItemSelectedListener true
            } else if (item.itemId == R.id.updates) {
                mediaPlayer?.pause()
                isPlaying = false

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

        musicName = findViewById(R.id.music_name)
        artist = findViewById(R.id.singer)
        duration = findViewById(R.id.duration)
        totalDuration = findViewById(R.id.total_duration)
        prev = findViewById(R.id.prev)
        playPause = findViewById(R.id.play)
        next = findViewById(R.id.next)
        progressIndicator = findViewById(R.id.progress_indicator)
        musicImage = findViewById(R.id.music_image)
        musicControl = findViewById(R.id.music_control)


        musicList = intent.getSerializableExtra("musicList") as ArrayList<Music>
        currentPosition = intent.getIntExtra("currentPosition", 0)

        playMusic(currentPosition)

        playPause.setOnClickListener {
            if (isPlaying) {
                pauseMusic()
            } else {
                resumeMusic()
            }
        }

        prev.setOnClickListener {
            playPrevious()
        }

        next.setOnClickListener {
            playNext()
        }

        progressIndicator.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val width = v.width
                val x = event.x
                val progress = (x / width * 100).toInt()
                progressIndicator.progress = progress
                val newPosition = ((progress * mediaPlayer?.duration!!) ?: 0) / 100
                mediaPlayer?.seekTo(newPosition)
            }
            true
        }

        musicControl.setOnClickListener {
            playbackMode = when (playbackMode) {
                PlaybackMode.REPEAT_ALL -> {
                    musicControl.setImageResource(R.drawable.baseline_repeat_one_24)
                    PlaybackMode.REPEAT_ONE
                }
                PlaybackMode.REPEAT_ONE -> {
                    musicControl.setImageResource(R.drawable.baseline_shuffle_24)
                    PlaybackMode.SHUFFLE
                }
                PlaybackMode.SHUFFLE -> {
                    musicControl.setImageResource(R.drawable.baseline_repeat_24)
                    PlaybackMode.REPEAT_ALL
                }
            }
        }
    }

    private fun playMusic(position: Int) {
        // Release any previously playing media
        mediaPlayer?.release()

        // Get the current music and update the UI with its details
        val music = musicList[position]
        musicName.text = music.title
        artist.text = music.artist

        // Load and display the music image if available
        loadMusicImage(music.filePath)

        // Initialize the MediaPlayer and start playing the selected music
        mediaPlayer = MediaPlayer().apply {
            setDataSource(music.filePath)
            prepare()
            start()
        }

        isPlaying = true
        playPause.setImageResource(R.drawable.baseline_pause_circle_outline_24)

        // Set a listener to automatically play the next song when the current one finishes
        mediaPlayer?.setOnCompletionListener {
            when (playbackMode) {
                PlaybackMode.REPEAT_ALL -> playNext()
                PlaybackMode.REPEAT_ONE -> mediaPlayer?.start()
                PlaybackMode.SHUFFLE -> playRandom()
            }
        }

        // Update the total duration of the song
        totalDuration.text = formatDuration(mediaPlayer?.duration ?: 0)

        // Start updating the current duration
        handler.post(updateDurationRunnable)
    }

    private fun playRandom() {
        currentPosition = Random.nextInt(musicList.size)
        playMusic(currentPosition)
    }

    private fun loadMusicImage(filePath: String) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)

        // Retrieve the embedded picture
        val art = retriever.embeddedPicture

        if (art != null) {
            val inputStream = ByteArrayInputStream(art)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            musicImage.setImageBitmap(bitmap)
        } else {
            // If no embedded picture is found, set a default image
            musicImage.setImageResource(R.drawable.baseline_music_note2_24)
        }

        retriever.release()
    }

    private fun pauseMusic() {
        // Pause the music and update the UI
        mediaPlayer?.pause()
        isPlaying = false
        playPause.setImageResource(R.drawable.outline_play_circle_24)
        handler.removeCallbacks(updateDurationRunnable)
    }

    private fun resumeMusic() {
        // Resume the music and update the UI
        mediaPlayer?.start()
        isPlaying = true
        playPause.setImageResource(R.drawable.baseline_pause_circle_outline_24)
        handler.post(updateDurationRunnable)
    }

    private fun playNext() {
        currentPosition = (currentPosition + 1) % musicList.size
        playMusic(currentPosition)
    }

    private fun playPrevious() {
        // Move to the previous song in the list and play it
        currentPosition = if (currentPosition - 1 < 0) {
            musicList.size - 1
        } else {
            currentPosition - 1
        }
        playMusic(currentPosition)
    }

    private fun formatDuration(durationInMillis: Int): String {
        // Convert milliseconds to minutes and seconds for display
        val minutes = durationInMillis / 1000 / 60
        val seconds = durationInMillis / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateDurationRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}