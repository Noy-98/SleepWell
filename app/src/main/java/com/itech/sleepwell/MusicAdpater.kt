package com.itech.sleepwell

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MusicAdapter(
    private val context: Context,
    private var musicList: List<Music>
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    private var playingPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false)
        return MusicViewHolder(view)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = musicList[position]
        holder.musicName.text = music.title
        holder.singer.text = music.artist

        holder.play.setImageResource(
            if (playingPosition == position) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24
        )

        holder.play.setOnClickListener {
            if (playingPosition == position) {
                pauseMusic()
            } else {
                playMusic(music, position)
            }
        }

        holder.itemView.setOnClickListener {
            stopMusic()
            val intent = Intent(context, PlayMusicDashboard::class.java).apply {
                putExtra("musicList", ArrayList(musicList))
                putExtra("currentPosition", position)
            }
            context.startActivity(intent)
        }
    }

    private fun stopMusic() {
        mediaPlayer?.release()
        mediaPlayer = null
        playingPosition = -1
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = musicList.size

    fun updateMusicList(newMusicList: List<Music>) {
        this.musicList = newMusicList
        notifyDataSetChanged() // Notify the adapter to refresh the list
    }

    private fun playMusic(music: Music, position: Int) {
        if (mediaPlayer != null) {
            pauseMusic()
        }
        mediaPlayer = MediaPlayer().apply {
            setDataSource(music.filePath)
            prepare()
            start()
        }
        playingPosition = position
        notifyDataSetChanged()
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
        mediaPlayer = null
        playingPosition = -1
        notifyDataSetChanged()
    }

    inner class MusicViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val musicName: TextView = view.findViewById(R.id.music_name)
        val singer: TextView = view.findViewById(R.id.singer)
        val play: ImageView = view.findViewById(R.id.play)
    }
}
