package com.cameraviewer.player

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer

class VlcPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var libVlc: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    private var isPlaying = false

    var onErrorCallback: (() -> Unit)? = null
    var onPlayingCallback: (() -> Unit)? = null

    init {
        holder.addCallback(this)
    }

    private fun initVlc() {
        if (libVlc != null) return
        val options = ArrayList<String>().apply {
            add("--rtsp-tcp")             // força TCP para RTSP (mais estável em Wi-Fi)
            add("--network-caching=600")  // buffer de rede 600ms
            add("--clock-jitter=0")
            add("--no-drop-late-frames")
            add("--no-skip-frames")
            add("-v")
        }
        libVlc = LibVLC(context, options)
        mediaPlayer = MediaPlayer(libVlc).apply {
            setEventListener { event ->
                when (event.type) {
                    MediaPlayer.Event.Playing -> {
                        isPlaying = true
                        onPlayingCallback?.invoke()
                    }
                    MediaPlayer.Event.EncounteredError -> {
                        isPlaying = false
                        onErrorCallback?.invoke()
                    }
                    MediaPlayer.Event.EndReached -> {
                        isPlaying = false
                        // Tenta reconectar automaticamente
                        currentUrl?.let { play(it) }
                    }
                }
            }
        }
    }

    fun play(url: String) {
        currentUrl = url
        initVlc()

        val mp = mediaPlayer ?: return
        mp.stop()

        val media = Media(libVlc, android.net.Uri.parse(url)).apply {
            setHWDecoderEnabled(true, false)
            addOption(":network-caching=600")
            addOption(":rtsp-tcp")
        }
        mp.media = media
        media.release()

        attachSurface()
        mp.play()
    }

    fun stop() {
        isPlaying = false
        mediaPlayer?.stop()
    }

    fun release() {
        stop()
        mediaPlayer?.detachViews()
        mediaPlayer?.release()
        libVlc?.release()
        mediaPlayer = null
        libVlc = null
    }

    private fun attachSurface() {
        if (holder.surface.isValid) {
            mediaPlayer?.attachViews(this, null, false, false)
        }
    }

    // SurfaceHolder.Callback
    override fun surfaceCreated(holder: SurfaceHolder) {
        currentUrl?.let { play(it) }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mediaPlayer?.attachViews(this, null, false, false)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mediaPlayer?.detachViews()
    }
}
