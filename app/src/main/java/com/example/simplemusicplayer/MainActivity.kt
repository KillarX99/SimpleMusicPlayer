package com.example.simplemusicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var noMusicTextView: TextView
    private lateinit var songsTextView: TextView
    private lateinit var searchView: SearchView
    private val songsList = ArrayList<AudioModel>()
    private val PERMISSION_REQUEST_CODE = 123
    private var currentPlayingIndex: Int? = null
    private var isAscendingOrder = true

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recycler_view)
        noMusicTextView = findViewById(R.id.no_songs_text)
        songsTextView = findViewById(R.id.songs_text)
        searchView = findViewById(R.id.searchView)

        checkPermissions()

        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.YEAR
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val songData = AudioModel(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getInt(5).toString())
                if (File(songData.path).exists()) {
                    songsList.add(songData)
                }
            }
        }

        // Ordenar la lista de canciones por título
        songsList.sortBy { it.title }

        val reorderButton: ImageButton = findViewById(R.id.reorder_button)
        reorderButton.setOnClickListener {
            // Cambiar el estado de ordenación
            isAscendingOrder = !isAscendingOrder
            // Guardar el título de la canción actual
            val currentSongTitle = songsList.getOrNull(MyMediaPlayer.currentIndex)?.title

            // Ordenar la lista de canciones por título
            if (isAscendingOrder) {
                songsList.sortBy { it.title }
            } else {
                songsList.sortByDescending { it.title }
            }

            // Encontrar la nueva posición de la canción actual en la lista ordenada
            val newCurrentIndex = songsList.indexOfFirst { it.title == currentSongTitle }

            // Actualizar el índice de reproducción actual
            MyMediaPlayer.currentIndex = newCurrentIndex

            // Notificar al adaptador que los datos han cambiado
            recyclerView.adapter?.notifyDataSetChanged()

            updateCurrentIndex()
        }

        currentPlayingIndex = MyMediaPlayer.currentIndex

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterSongs(newText)
                return true
            }
        })

        if (songsList.isEmpty()) {
            noMusicTextView.visibility = View.VISIBLE
        } else {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = MusicListAdapter(songsList, applicationContext)
            songsTextView.visibility = View.VISIBLE
            songsTextView.text = "${songsList.size} Canciones"
        }
    }

    private fun filterSongs(query: String?) {
        val filteredSongs = if (query.isNullOrBlank()) {
            songsList.toList()
        } else {
            songsList.filter { it.title.contains(query, true) }
        }

        (recyclerView.adapter as? MusicListAdapter)?.apply {
            MyMediaPlayer.currentIndex = -1
            updateCurrentIndex()
            setFilteredSongs(filteredSongs)
            notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Requiere permiso de lectura", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
    }

    private fun updateCurrentIndex() {
        recyclerView.adapter?.let { adapter ->
            if (adapter is MusicListAdapter) {
                adapter.setCurrentPlayingIndex(MyMediaPlayer.currentIndex)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateCurrentIndex()
        searchView.clearFocus()
    }
}


