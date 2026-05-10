package com.cameraviewer.data

data class CameraConfig(
    val id: Int,
    val nome: String = "",
    val ip: String = "",
    val porta: Int = 554,
    val usuario: String = "admin",
    val senha: String = "",
    val marca: Marca = Marca.INTELBRAS,
    val urlCustom: String = "",
    val habilitada: Boolean = true
)

enum class Marca(val display: String) {
    INTELBRAS("Intelbras"),
    TAPO("Tapo (TP-Link)"),
    MIBO("Mibo"),
    CUSTOM("URL Personalizada")
}
