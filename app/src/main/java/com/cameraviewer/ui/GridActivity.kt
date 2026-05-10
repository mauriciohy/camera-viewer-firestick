package com.cameraviewer.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cameraviewer.R
import com.cameraviewer.data.CameraConfig
import com.cameraviewer.data.PrefsRepository
import com.cameraviewer.databinding.ActivityGridBinding
import com.cameraviewer.player.RtspHelper
import com.cameraviewer.player.VlcPlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GridActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGridBinding
    private lateinit var prefs: PrefsRepository

    // Mapeamento câmera -> índice 0..3
    private val players = mutableListOf<VlcPlayerView?>()
    private val statusLabels = mutableListOf<TextView?>()
    private var cameras = listOf<CameraConfig>()

    private var focusedCamera = 0   // qual câmera está focada (D-pad)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsRepository.getInstance(this)

        players.addAll(listOf(
            binding.player1,
            binding.player2,
            binding.player3,
            binding.player4
        ))

        statusLabels.addAll(listOf(
            binding.tvStatus1,
            binding.tvStatus2,
            binding.tvStatus3,
            binding.tvStatus4
        ))

        // Botão menu / configurações
        binding.btnConfig.setOnClickListener { abrirConfiguracoes() }
        binding.btnConfig.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                // Foca no grid ao pressionar cima no botão
                players[focusedCamera]?.requestFocus()
                true
            } else false
        }

        setupGridFocus()
    }

    override fun onResume() {
        super.onResume()
        cameras = prefs.getCameras()
        iniciarStreams()
    }

    override fun onPause() {
        super.onPause()
        players.forEach { it?.stop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        players.forEach { it?.release() }
    }

    private fun setupGridFocus() {
        val cells = listOf(
            binding.cell1, binding.cell2,
            binding.cell3, binding.cell4
        )

        cells.forEachIndexed { idx, cell ->
            cell.isFocusable = true
            cell.isFocusableInTouchMode = true
            cell.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    focusedCamera = idx
                    cell.setBackgroundResource(R.drawable.bg_camera_focused)
                } else {
                    cell.setBackgroundResource(R.drawable.bg_camera_normal)
                }
            }
            cell.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_CENTER,
                        KeyEvent.KEYCODE_ENTER -> {
                            abrirFullscreen(idx)
                            true
                        }
                        else -> false
                    }
                } else false
            }
        }

        // Configura navegação D-pad entre células
        binding.cell1.nextFocusRightId = R.id.cell2
        binding.cell1.nextFocusDownId  = R.id.cell3

        binding.cell2.nextFocusLeftId = R.id.cell1
        binding.cell2.nextFocusDownId = R.id.cell4

        binding.cell3.nextFocusRightId = R.id.cell4
        binding.cell3.nextFocusUpId    = R.id.cell1
        binding.cell3.nextFocusDownId  = R.id.btnConfig

        binding.cell4.nextFocusLeftId = R.id.cell3
        binding.cell4.nextFocusUpId   = R.id.cell2
        binding.cell4.nextFocusDownId = R.id.btnConfig

        binding.cell1.requestFocus()
    }

    private fun iniciarStreams() {
        cameras.forEachIndexed { idx, cam ->
            if (idx >= 4) return@forEachIndexed

            val player = players[idx] ?: return@forEachIndexed
            val statusTv = statusLabels[idx]

            statusTv?.text = cam.nome.ifEmpty { "Câmera ${cam.id}" }
            statusTv?.visibility = View.VISIBLE

            if (!cam.habilitada || cam.ip.isEmpty()) {
                statusTv?.text = "${cam.nome}\n[Não configurada]"
                return@forEachIndexed
            }

            val url = RtspHelper.buildRtspUrl(cam)

            player.onPlayingCallback = {
                runOnUiThread { statusTv?.visibility = View.GONE }
            }
            player.onErrorCallback = {
                runOnUiThread {
                    statusTv?.text = "${cam.nome}\n[Erro de conexão]"
                    statusTv?.visibility = View.VISIBLE
                }
                // Tenta reconectar após 5s
                lifecycleScope.launch {
                    delay(5_000)
                    player.play(url)
                }
            }

            player.play(url)
        }
    }

    private fun abrirFullscreen(cameraIdx: Int) {
        val cam = cameras.getOrNull(cameraIdx) ?: return
        val url = RtspHelper.buildRtspUrl(cam)
        val intent = Intent(this, FullscreenActivity::class.java).apply {
            putExtra(FullscreenActivity.EXTRA_URL, url)
            putExtra(FullscreenActivity.EXTRA_NOME, cam.nome.ifEmpty { "Câmera ${cam.id}" })
        }
        // Pausa os outros streams para liberar recursos
        players.forEach { it?.stop() }
        startActivity(intent)
    }

    private fun abrirConfiguracoes() {
        players.forEach { it?.stop() }
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            abrirConfiguracoes()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
