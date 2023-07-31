package com.example.simplemusicapp.screen.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.simplemusicapp.data.model.Song
import com.example.simplemusicapp.databinding.LayoutItemSongBinding
import com.example.simplemusicapp.R

class SongAdapter(
    private val context: Context,
    private val itemClickListener: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongsViewHolder>() {

    private var listSongs = ArrayList<Song>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val binding = LayoutItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listSongs.size
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        val songData = listSongs[position]
        holder.bindData(songData, itemClickListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(songs: ArrayList<Song>) {
        listSongs = songs
        notifyDataSetChanged()
    }

    private fun checkCurrentMode(): Int {
        // Check current Night Mode and set background color for RelativeLayout
        val nightMode = AppCompatDelegate.getDefaultNightMode()
        val backgroundColor = if (nightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            ContextCompat.getColor(context, R.color.nightBackground)
        } else {
            ContextCompat.getColor(context, R.color.gray) // Default color in Light Mode
        }
        return backgroundColor
    }

    inner class SongsViewHolder(private val binding: LayoutItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(song: Song, itemClickListener: (Song) -> Unit) {
            binding.tvNameSong.text = song.title
            binding.tvNameSinger.text = song.artist
            binding.relativeLayoutSong.setBackgroundColor(checkCurrentMode())

            Glide.with(context)
                .load(song.image) // Use the album artwork URI from the Song object
                .placeholder(R.drawable.music_icon) // Replace with your default placeholder image resource
                .error(R.drawable.music_icon) // Replace with your error image resource
                .into(binding.imgSong)

            binding.relativeLayoutSong.setOnClickListener {
                itemClickListener(song)
            }
        }
    }
}