package com.example.simplemusicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.simplemusicplayer.MyMediaPlayer.currentIndex
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifImageView
import java.io.IOException
import java.util.concurrent.TimeUnit


class MusicPlayerActivity : AppCompatActivity() {
    private lateinit var titleTv: TextView
    private lateinit var currentTimeTv: TextView
    private lateinit var totalTimeTv: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var pausePlay: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var previousBtn: ImageView
    private lateinit var musicIcon: ImageView
    private lateinit var songsList: Array<AudioModel>
    private lateinit var currentSong: AudioModel
    private lateinit var backgroundGif: GifImageView
    private val mediaPlayer: MediaPlayer = MyMediaPlayer.instance
    private var x = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        backgroundGif = findViewById(R.id.background_gif)
        (backgroundGif.drawable as GifDrawable).start()

        titleTv = findViewById(R.id.song_title)
        currentTimeTv = findViewById(R.id.current_time)
        totalTimeTv = findViewById(R.id.total_time)
        seekBar = findViewById(R.id.seek_bar)
        pausePlay = findViewById(R.id.pause_play)
        nextBtn = findViewById(R.id.next)
        previousBtn = findViewById(R.id.previous)
        musicIcon = findViewById(R.id.music_icon_big)

        titleTv.isSelected = true

        songsList = intent.getSerializableExtra("LIST") as? Array<AudioModel>
            ?: throw IllegalArgumentException("Song list must not be null")

        setResourcesWithMusic()

        val handler = Handler()
        val updateSeekBar = object : Runnable {
            override fun run() {
                if (mediaPlayer.isPlaying) {
                    val currentPosition = mediaPlayer.currentPosition
                    seekBar.progress = currentPosition
                    currentTimeTv.text = convertToMMSS(currentPosition.toString())
                }
                handler.postDelayed(this, 1000) // Actualizar cada segundo
            }
        }
        handler.postDelayed(updateSeekBar, 0)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer.isPlaying && fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        pausePlay.setOnClickListener {
            pausePlay()
        }

        nextBtn.setOnClickListener {
            playNextSong()
        }

        previousBtn.setOnClickListener {
            playPreviousSong()
        }

        // Configuración para reproducir la siguiente canción automáticamente
        mediaPlayer.setOnCompletionListener {
            playNextSong()
        }
    }

    private fun setResourcesWithMusic() {
        currentSong = songsList[MyMediaPlayer.currentIndex]

        titleTv.text = currentSong.title
        currentTimeTv.text = "0:00" // Restablecer el tiempo actual
        totalTimeTv.text = convertToMMSS(currentSong.duration)

        // Mostrar la información del artista, álbum y año
        findViewById<TextView>(R.id.artist).text = currentSong.artist
        findViewById<TextView>(R.id.album).text = currentSong.album
        findViewById<TextView>(R.id.year).text = currentSong.year

        if (MyMediaPlayer.currentIndex == MyMediaPlayer.currentIndex && mediaPlayer.isPlaying) {
            // La canción seleccionada es la misma que la actualmente en reproducción
            pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            seekBar.max = mediaPlayer.duration
            seekBar.progress = mediaPlayer.currentPosition
            updateSeekBar()
        } else {
            // La canción seleccionada es diferente a la actualmente en reproducción
            MyMediaPlayer.instance.reset()
            MyMediaPlayer.instance.setDataSource(currentSong.path)
            MyMediaPlayer.instance.prepare()
            MyMediaPlayer.instance.start()
            pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
            seekBar.max = mediaPlayer.duration
            updateSeekBar()
        }
    }

    private fun updateSeekBar() {
        val handler = Handler()
        val updateSeekBar = object : Runnable {
            override fun run() {
                if (MyMediaPlayer.instance.isPlaying) {
                    val currentPosition = MyMediaPlayer.instance.currentPosition
                    seekBar.progress = currentPosition
                    currentTimeTv.text = convertToMMSS(currentPosition.toString())
                    handler.postDelayed(this, 1000) // Actualizar cada segundo
                }
            }
        }
        handler.postDelayed(updateSeekBar, 0)
    }

    private fun playNextSong() {
        if (MyMediaPlayer.currentIndex < songsList.size - 1) {
            MyMediaPlayer.currentIndex++
            mediaPlayer.reset()
            setResourcesWithMusic()
        }
    }

    private fun playPreviousSong() {
        if (MyMediaPlayer.currentIndex > 0) {
            MyMediaPlayer.currentIndex--
            mediaPlayer.reset()
            setResourcesWithMusic()
        }
    }

    private fun pausePlay() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            pausePlay.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        } else {
            mediaPlayer.start()
            pausePlay.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24)
        }
    }

    companion object {
        fun convertToMMSS(duration: String): String {
            val millis = duration.toLong()
            return String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
            )
        }
    }
}

