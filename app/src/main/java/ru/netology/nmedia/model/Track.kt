package ru.netology.nmedia.model

data class Track (
    val itemId: Long = 0,
    val isPlaying: Boolean = false,
    val url: String = "",
)