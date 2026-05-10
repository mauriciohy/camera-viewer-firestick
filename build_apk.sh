#!/bin/bash
# Script para compilar e instalar via ADB
set -e

PACKAGE="com.cameraviewer"

echo "=== Compilando APK de debug ==="
./gradlew assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"

echo ""
echo "=== APK gerado: $APK ==="
echo ""

# Se um dispositivo ADB estiver conectado, instala automaticamente
if adb devices | grep -q "device$"; then
    echo "=== Instalando via ADB ==="
    adb install -r "$APK"
    echo "=== Iniciando app ==="
    adb shell am start -n "$PACKAGE/.ui.LoginActivity"
else
    echo "Nenhum dispositivo ADB conectado."
    echo ""
    echo "Para instalar no Fire Stick:"
    echo "  1. Ative o modo de desenvolvedor no Fire Stick"
    echo "  2. adb connect <IP_DO_FIRESTICK>:5555"
    echo "  3. adb install $APK"
    echo ""
    echo "  Ou copie o APK para o Fire Stick e instale com o app Downloader"
fi
