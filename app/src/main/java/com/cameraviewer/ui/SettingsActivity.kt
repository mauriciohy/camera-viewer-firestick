package com.cameraviewer.ui

import android.os.Bundle
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cameraviewer.data.CameraConfig
import com.cameraviewer.data.Marca
import com.cameraviewer.data.PrefsRepository
import com.cameraviewer.databinding.ActivitySettingsBinding
import com.cameraviewer.player.RtspHelper

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PrefsRepository
    private var cameras = mutableListOf<CameraConfig>()

    // câmera selecionada no momento
    private var camSelecionada = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsRepository.getInstance(this)
        cameras = prefs.getCameras().toMutableList()

        setupSpinnerMarca()
        setupCameraSelector()
        carregarCamera(0)

        binding.btnSalvar.setOnClickListener { salvar() }
        binding.btnTestar.setOnClickListener { testarConexao() }
        binding.btnSalvarPin.setOnClickListener { salvarPin() }

        // PIN atual
        binding.etPin.setText(prefs.getAppPin())
    }

    private fun setupSpinnerMarca() {
        val marcas = Marca.values().map { it.display }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, marcas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMarca.adapter = adapter

        binding.spinnerMarca.setOnItemSelectedListener(object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                val marca = Marca.values()[pos]
                binding.tilUrlCustom.visibility =
                    if (marca == Marca.CUSTOM) android.view.View.VISIBLE else android.view.View.GONE
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun setupCameraSelector() {
        val camNames = (1..4).map { "Câmera $it" }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, camNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCamera.adapter = adapter

        binding.spinnerCamera.setOnItemSelectedListener(object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                salvarCameraAtual()
                camSelecionada = pos
                carregarCamera(pos)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        })
    }

    private fun carregarCamera(idx: Int) {
        val cam = cameras.getOrNull(idx) ?: return
        binding.etNome.setText(cam.nome)
        binding.etIp.setText(cam.ip)
        binding.etPorta.setText(cam.porta.toString())
        binding.etUsuario.setText(cam.usuario)
        binding.etSenha.setText(cam.senha)
        binding.etUrlCustom.setText(cam.urlCustom)
        binding.spinnerMarca.setSelection(cam.marca.ordinal)
        binding.switchHabilitada.isChecked = cam.habilitada
    }

    private fun salvarCameraAtual() {
        val cam = cameras.getOrNull(camSelecionada) ?: return
        cameras[camSelecionada] = cam.copy(
            nome      = binding.etNome.text.toString().trim(),
            ip        = binding.etIp.text.toString().trim(),
            porta     = binding.etPorta.text.toString().toIntOrNull() ?: 554,
            usuario   = binding.etUsuario.text.toString().trim(),
            senha     = binding.etSenha.text.toString(),
            marca     = Marca.values()[binding.spinnerMarca.selectedItemPosition],
            urlCustom = binding.etUrlCustom.text.toString().trim(),
            habilitada = binding.switchHabilitada.isChecked
        )
    }

    private fun salvar() {
        salvarCameraAtual()
        prefs.saveCameras(cameras)
        Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun salvarPin() {
        val novoPin = binding.etPin.text.toString().trim()
        if (novoPin.length < 4) {
            Toast.makeText(this, "PIN deve ter ao menos 4 dígitos", Toast.LENGTH_SHORT).show()
            return
        }
        prefs.setAppPin(novoPin)
        Toast.makeText(this, "PIN atualizado", Toast.LENGTH_SHORT).show()
    }

    private fun testarConexao() {
        salvarCameraAtual()
        val cam = cameras.getOrNull(camSelecionada) ?: return
        val url = if (cam.marca == Marca.CUSTOM) cam.urlCustom else RtspHelper.buildRtspUrl(cam)
        Toast.makeText(this, "URL: $url", Toast.LENGTH_LONG).show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            salvar()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
