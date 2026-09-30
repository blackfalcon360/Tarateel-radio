package com.blackradio.tarateel

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture

class MainActivity : AppCompatActivity() {

    companion object {
        // Asli stream URL (telegradio page ke andar se nikala gaya)
        const val STREAM_URL = "https://qurango.net/radio/tarateel"
        const val STATION_NAME = "أبــو الـخــيـــر الأثـــري"
    }

    private lateinit var statusText: TextView
    private lateinit var playButton: Button
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController? get() =
        if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = updateUi()
        override fun onPlaybackStateChanged(playbackState: Int) = updateUi()
        override fun onPlayerError(error: PlaybackException) {
            statusText.text = getString(R.string.error_stream)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.stationName).text = STATION_NAME
        statusText = findViewById(R.id.statusText)
        playButton = findViewById(R.id.playButton)

        playButton.setOnClickListener {
            val c = controller ?: return@setOnClickListener
            if (c.isPlaying || c.playWhenReady) {
                c.pause()
            } else {
                if (c.playbackState == Player.STATE_IDLE || c.playbackState == Player.STATE_ENDED) {
                    c.prepare()
                }
                c.play()
            }
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onStart() {
        super.onStart()
        val token = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        val future = MediaController.Builder(this, token).buildAsync()
        controllerFuture = future
        future.addListener({
            val c = controller ?: return@addListener
            c.addListener(listener)
            if (c.mediaItemCount == 0) {
                val item = MediaItem.Builder()
                    .setUri(STREAM_URL)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(STATION_NAME)
                            .setArtist("Saudi Arabia")
                            .build()
                    )
                    .build()
                c.setMediaItem(item)
                c.prepare()
            }
            updateUi()
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onStop() {
        controller?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        super.onStop()
    }

    private fun updateUi() {
        val c = controller ?: return
        when {
            c.isPlaying -> {
                statusText.text = getString(R.string.status_playing)
                playButton.text = getString(R.string.btn_pause)
            }
            c.playWhenReady && c.playbackState == Player.STATE_BUFFERING -> {
                statusText.text = getString(R.string.status_buffering)
                playButton.text = getString(R.string.btn_pause)
            }
            else -> {
                statusText.text = getString(R.string.status_stopped)
                playButton.text = getString(R.string.btn_play)
            }
        }
    }
}
