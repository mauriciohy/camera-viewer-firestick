package com.cameraviewer.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefsRepository(context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "camera_viewer_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback para SharedPreferences normal caso criptografia falhe
            context.getSharedPreferences("camera_viewer_prefs_plain", Context.MODE_PRIVATE)
        }
    }

    private val gson = Gson()

    companion object {
        private const val KEY_APP_PIN = "app_pin"
        private const val KEY_CAMERAS = "cameras"
        private const val DEFAULT_PIN = "1234"

        @Volatile
        private var instance: PrefsRepository? = null

        fun getInstance(context: Context): PrefsRepository =
            instance ?: synchronized(this) {
                instance ?: PrefsRepository(context.applicationContext).also { instance = it }
            }
    }

    fun getAppPin(): String = prefs.getString(KEY_APP_PIN, DEFAULT_PIN) ?: DEFAULT_PIN

    fun setAppPin(pin: String) = prefs.edit().putString(KEY_APP_PIN, pin).apply()

    fun getCameras(): List<CameraConfig> {
        val json = prefs.getString(KEY_CAMERAS, null)
        return if (json != null) {
            val type = object : TypeToken<List<CameraConfig>>() {}.type
            gson.fromJson(json, type) ?: defaultCameras()
        } else {
            defaultCameras()
        }
    }

    fun saveCameras(cameras: List<CameraConfig>) {
        prefs.edit().putString(KEY_CAMERAS, gson.toJson(cameras)).apply()
    }

    fun saveCamera(camera: CameraConfig) {
        val cameras = getCameras().toMutableList()
        val idx = cameras.indexOfFirst { it.id == camera.id }
        if (idx >= 0) cameras[idx] = camera else cameras.add(camera)
        saveCameras(cameras)
    }

    private fun defaultCameras(): List<CameraConfig> = (1..4).map { id ->
        CameraConfig(
            id = id,
            nome = "Câmera $id",
            ip = "192.168.1.${100 + id}",
            porta = 554,
            usuario = "admin",
            senha = "",
            marca = Marca.INTELBRAS
        )
    }
}
