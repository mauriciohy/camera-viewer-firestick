package com.cameraviewer.player

import com.cameraviewer.data.CameraConfig
import com.cameraviewer.data.Marca

object RtspHelper {

    fun buildRtspUrl(cfg: CameraConfig): String = when (cfg.marca) {
        Marca.INTELBRAS ->
            "rtsp://${encode(cfg.usuario)}:${encode(cfg.senha)}@${cfg.ip}:${cfg.porta}" +
            "/cam/realmonitor?channel=1&subtype=0"

        Marca.TAPO ->
            "rtsp://${encode(cfg.usuario)}:${encode(cfg.senha)}@${cfg.ip}:${cfg.porta}/stream1"

        Marca.MIBO ->
            "rtsp://${encode(cfg.usuario)}:${encode(cfg.senha)}@${cfg.ip}:${cfg.porta}/livestream/0"

        Marca.CUSTOM -> cfg.urlCustom
    }

    // Encode de caracteres especiais na URL (@ e : são os mais críticos)
    private fun encode(s: String): String = s
        .replace("%", "%25")
        .replace("@", "%40")
        .replace(":", "%3A")
        .replace(" ", "%20")
        .replace("#", "%23")
        .replace("?", "%3F")
}
