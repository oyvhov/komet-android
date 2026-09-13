<#
.SYNOPSIS
    Startar den eigne Komet-emulatoren (Komet_Phone) og installerer siste debug-APK.

.DESCRIPTION
    Komet har sin eigen AVD i C:\LeseApp\.avd, på port 5580, så Spole sine profilar
    (5560, 5562, 5564) aldri blir rørte. Emulatoren blir starta gjennom ASCII-koplinga
    C:\Android\sdk; stien med «Ø» får emulatoren til å krasje under oppstart.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Start-KometEmulator.ps1
    powershell -NoProfile -ExecutionPolicy Bypass -File C:\LeseApp\scripts\Start-KometEmulator.ps1 -Headless
#>
param(
    [switch]$Headless,
    [switch]$SkipInstall
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$sdk = 'C:\Android\sdk'
$adb = Join-Path $sdk 'platform-tools\adb.exe'
$serial = 'emulator-5580'

$env:ANDROID_AVD_HOME = Join-Path $root '.avd'
$env:ANDROID_SDK_ROOT = $sdk
$env:ANDROID_HOME = $sdk

$running = (& $adb devices) -match $serial
if (-not $running) {
    $arguments = @('-avd', 'Komet_Phone', '-port', '5580', '-no-boot-anim', '-gpu', 'auto')
    if ($Headless) { $arguments += @('-no-window', '-no-audio') }
    Start-Process -FilePath (Join-Path $sdk 'emulator\emulator.exe') -ArgumentList $arguments `
        -RedirectStandardOutput (Join-Path $root '.avd\emulator-out.log') `
        -RedirectStandardError (Join-Path $root '.avd\emulator-err.log') -WindowStyle Hidden | Out-Null
    Write-Host 'Ventar på at emulatoren skal starte …'
    & $adb -s $serial wait-for-device
    do {
        Start-Sleep -Seconds 3
        $booted = ((& $adb -s $serial shell getprop sys.boot_completed) -join '').Trim()
    } until ($booted -eq '1')
}

if (-not $SkipInstall) {
    $apk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
    & $adb -s $serial install -r $apk
    & $adb -s $serial shell monkey -p app.komet.debug -c android.intent.category.LAUNCHER 1 | Out-Null
}
Write-Host "Klar: $serial"
