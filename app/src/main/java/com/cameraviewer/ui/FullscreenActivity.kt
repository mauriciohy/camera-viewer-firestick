package com.cameraviewer.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.cameraviewer.databinding.ActivityFullscreenBinding
import com.cameraviewer.player.VlcPlayerView

class FullscreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenBinding
    private var player: VlcPlayerView? = null

    companion object {
        const val EXTRA_URL  = "extra_url"
        const val EXTRA_NOME = "extra_nome"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url  = intent.getStringExtra(EXTRA_URL) ?: run { finish(); return }
        val nome = intent.getStringExtra(EXTRA_NOME) ?: "Câmera"

        binding.tvNome.text = nome
        binding.tvStatus.text = "Conectando..."
        binding.tvStatus.visibility = View.VISIBLE

        player = binding.playerFullscreen

        player?.onPlayingCallback = {
            runOnUiThread {
                binding.tvStatus.visibility = View.GONE
                binding.tvNome.visibility   = View.VISIBLE
            }
        }
        player?.onErrorCallback = {
            runOnUiThread {
                binding.tvStatus.text = "Erro de conexão — reconectando..."
            }
        }

        player?.play(url)

        // Oculta a barra de nome após 3s
        binding.root.postDelayed({
            binding.tvNome.animate().alpha(0f).setDuration(500).withEndAction {
                binding.tvNome.visibility = View.GONE
            }.start()
        }, 3_000)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
