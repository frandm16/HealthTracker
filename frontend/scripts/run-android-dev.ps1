$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$androidStudioJbr = 'C:\Program Files\Android\Android Studio\jbr'
$androidSdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'

if (-not (Test-Path $androidStudioJbr)) {
  throw "No se encontro Java 21 de Android Studio en: $androidStudioJbr"
}

if (-not (Test-Path $androidSdk)) {
  throw "No se encontro el Android SDK en: $androidSdk"
}

$env:JAVA_HOME = $androidStudioJbr
$env:ANDROID_HOME = $androidSdk
$env:ANDROID_SDK_ROOT = $androidSdk
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\emulator;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:Path"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "ANDROID_HOME=$env:ANDROID_HOME"

Push-Location $projectRoot
try {
  & 'C:\Program Files\nodejs\npx.cmd' expo run:android
} finally {
  Pop-Location
}
