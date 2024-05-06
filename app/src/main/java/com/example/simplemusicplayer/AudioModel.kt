package com.example.simplemusicplayer

import java.io.Serializable

data class AudioModel(
    val title: String,
    val path: String,
    val duration: String,
    val artist: String,
    val album: String,
    val year: String
) : Serializable