package ru.netology.nmedia

import android.media.MediaPlayer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class MediaLifecycleObserver : LifecycleEventObserver {
    var player: MediaPlayer? = MediaPlayer()
    private var position: Int? = 0
    private var trackUrl: String = ""

    fun pause(){
        player?.pause()
        position = player?.currentPosition
        player?.reset()
    }

    fun play(url: String){
        trackUrl = url
        player?.setDataSource(url)
        player?.setOnPreparedListener {
            it.start()
        }
        player?.prepareAsync()
    }

    fun resume(){
        player?.setDataSource(trackUrl)
        player?.setOnPreparedListener {
            position?.let { savedPosition -> it.seekTo(savedPosition) }
            it.start()
        }
        player?.prepareAsync()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_PAUSE -> player?.pause()
            Lifecycle.Event.ON_STOP -> {
                player?.release()
                player = null
            }
            Lifecycle.Event.ON_DESTROY -> source.lifecycle.removeObserver(this)
            else -> Unit
        }
    }
}
