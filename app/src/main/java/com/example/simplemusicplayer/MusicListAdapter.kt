package com.example.simplemusicplayer

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MusicListAdapter(private var songsList: List<AudioModel>, private val context: Context) :
    RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {

    private var currentPlayingIndex: Int? = null

    fun setCurrentPlayingIndex(index: Int?) {
        currentPlayingIndex = index
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songData = songsList[position]
        holder.titleTextView.text = songData.title

        if (position == currentPlayingIndex) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.highlighted_background))
            holder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.highlighted_text))
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.default_background))
            holder.titleTextView.setTextColor(ContextCompat.getColor(context, R.color.default_text))
        }


        holder.itemView.setOnClickListener {
            if (MyMediaPlayer.currentIndex != position) {
                MyMediaPlayer.instance.reset()
                MyMediaPlayer.currentIndex = position
            }

            val intent = Intent(context, MusicPlayerActivity::class.java)
            intent.putExtra("LIST", songsList.toTypedArray())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    fun setFilteredSongs(filteredSongs: List<AudioModel>) {
        val mutableSongsList = songsList.toMutableList()
        mutableSongsList.clear()
        mutableSongsList.addAll(filteredSongs)
        songsList = mutableSongsList.toList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.music_title_text)
    }
}

