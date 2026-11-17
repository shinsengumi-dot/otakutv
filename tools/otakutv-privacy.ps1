# OtakuTV privacy and speed tweaks for an Android TV box, with an exact undo.
#
# Usage: tools\otakutv-privacy.ps1 -TvHost 192.168.1.50          apply
#        tools\otakutv-privacy.ps1 -TvHost 192.168.1.50 -Undo    put back the values saved when applying
#
# Applies: crash reports to Google off (secure send_action_app_error 0), AdGuard DNS as Private DNS
# (blocks ad networks for every app; not app analytics such as Firebase), system animations at 0.5x.
# The previous values are saved in tools\otakutv-privacy-<host>.json (git-ignored) before anything changes.
param(
    [string]$TvHost = $env:TV_HOST,
    [switch]$Undo
)
$ErrorActionPreference = 'Stop'
if (-not $TvHost) { throw 'pass -TvHost <box ip> or set $env:TV_HOST. Nothing was sent to the box.' }
$env:TV_HOST = $TvHost
$py = Join-Path $PSScriptRoot 'venv\Scripts\python.exe'
$tv = Join-Path $PSScriptRoot 'tv.py'
if (-not (Test-Path $py)) { throw 'run: python -m venv tools\venv; tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt' }
$saved = Join-Path $PSScriptRoot ("otakutv-privacy-" + ($TvHost -replace '[^0-9A-Za-z.-]', '_') + '.json')

$keys = @(
    @{ ns = 'secure'; key = 'send_action_app_error'; value = '0' },
    @{ ns = 'global'; key = 'private_dns_specifier'; value = 'dns.adguard-dns.com' },
    @{ ns = 'global'; key = 'private_dns_mode'; value = 'hostname' },
    @{ ns = 'global'; key = 'window_animation_scale'; value = '0.5' },
    @{ ns = 'global'; key = 'transition_animation_scale'; value = '0.5' },
    @{ ns = 'global'; key = 'animator_duration_scale'; value = '0.5' }
)

function Get-Setting($ns, $key) {
    ((& $py -I $tv "settings get $ns $key") -join '').Trim()
}

if ($Undo) {
    if (-not (Test-Path $saved)) { throw "no saved values in ${saved}: nothing to undo. Nothing was sent to the box." }
    $before = Get-Content $saved -Raw | ConvertFrom-Json
    foreach ($k in $keys) {
        $old = $before.($k.ns + '/' + $k.key)
        if ($null -eq $old -or $old -eq 'null' -or $old -eq '') {
            & $py -I $tv "settings delete $($k.ns) $($k.key)" | Out-Null
        } else {
            & $py -I $tv "settings put $($k.ns) $($k.key) $old" | Out-Null
        }
    }
    Remove-Item $saved
    Write-Host 'Privacy tweaks undone; the previous values are back.'
    return
}

if (-not (Test-Path $saved)) {
    $before = [ordered]@{}
    foreach ($k in $keys) { $before[$k.ns + '/' + $k.key] = Get-Setting $k.ns $k.key }
    $before | ConvertTo-Json | Out-File -Encoding utf8 $saved
}
foreach ($k in $keys) {
    & $py -I $tv "settings put $($k.ns) $($k.key) $($k.value)" | Out-Null
}
$check = ((& $py -I $tv 'ping -c1 -W3 doubleclick.net 2>&1 | head -1') -join '')
Write-Host "Applied. doubleclick.net now resolves as: $check"
Write-Host "Undo with: tools\otakutv-privacy.ps1 -TvHost $TvHost -Undo"
