package com.cameraviewer.ui

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cameraviewer.data.PrefsRepository
import com.cameraviewer.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var prefs: PrefsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsRepository.getInstance(this)

        binding.btnEntrar.setOnClickListener { tentarLogin() }

        binding.etSenha.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                tentarLogin()
                true
            } else false
        }

        binding.btnEntrar.requestFocus()
    }

    private fun tentarLogin() {
        val digitado = binding.etSenha.text.toString()
        if (digitado == prefs.getAppPin()) {
            startActivity(Intent(this, GridActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show()
            binding.etSenha.text?.clear()
        }
    }
}
