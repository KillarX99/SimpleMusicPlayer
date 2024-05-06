package com.example.simplemusicplayer

import android.media.MediaPlayer

object MyMediaPlayer {
    val instance: MediaPlayer by lazy { MediaPlayer() }
    var currentIndex: Int = -1
}