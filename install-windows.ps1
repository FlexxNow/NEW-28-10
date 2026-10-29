param([string]$TvHost = '192.168.1.40')
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
if (-not $env:JAVA_HOME -or -not (Test-Path "$env:JAVA_HOME\bin\javac.exe")) {
    $jdk = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like 'jdk-17*' -and (Test-Path "$($_.FullName)\bin\javac.exe") } |
        Select-Object -First 1
    if (-not $jdk) { throw 'No encuentro JDK 17. Establece JAVA_HOME al JDK instalado.' }
    $env:JAVA_HOME = $jdk.FullName
}
if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = "$env:USERPROFILE\Android\Sdk" }
$adb = Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
if (-not (Test-Path $adb)) { throw 'No encuentro adb.exe dentro de ANDROID_HOME\platform-tools.' }
& .\gradlew.bat --no-daemon :app:assembleDebug :app:lintDebug --console=plain
if ($LASTEXITCODE -ne 0) { throw 'La compilación o lint fallaron; no se instaló nada.' }
& $adb connect "${TvHost}:5555"
if ($LASTEXITCODE -ne 0) { throw 'No se pudo conectar a la TV.' }
& $adb -s "${TvHost}:5555" get-state
if ($LASTEXITCODE -ne 0) { throw 'La TV no está autorizada o no responde. Revisa la autorización en pantalla.' }
& $adb -s "${TvHost}:5555" install -r '.\app\build\outputs\apk\debug\app-debug.apk'
if ($LASTEXITCODE -ne 0) { throw 'No se pudo instalar. Conserva el error; no desactives la verificación de paquetes.' }
& $adb -s "${TvHost}:5555" shell am start -n com.flexxnow.tvalarm/.MainActivity
if ($LASTEXITCODE -ne 0) { throw 'No se pudo abrir la app.' }
Write-Host 'Instalada Alarma TV. Primero prueba el sonido con volumen bajo y luego una alarma a 2 minutos desde reposo.'
