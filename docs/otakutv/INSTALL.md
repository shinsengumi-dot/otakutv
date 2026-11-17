# Install OtakuTV

[Français](INSTALL.fr.md) · [Back to README](../../README.md)

About an hour the first time, most of it waiting for builds. Every step can be undone; the last section says how.
The commands are for Windows PowerShell; on macOS and Linux use the `.sh` twin of each script and
`tools/venv/bin/python` instead of `tools\venv\Scripts\python.exe`.

## 1. What you need

- **The box**: Android TV 8 or later. Note its IP address (Settings > Network).
- **A computer** on the same network, with:
  - [Git](https://git-scm.com/) and [Python 3.10+](https://www.python.org/);
  - **JDK 17** and the **Android SDK** (platform 34, build-tools 34.0.0). The simplest way is
    [Android Studio](https://developer.android.com/studio); the build downloads the missing SDK parts itself
    once the licences are accepted.

## 2. Get the code (keep the path short)

Windows limits paths to 260 characters, and this repository has deep folders. Clone it close to the drive root:

```powershell
git clone -c core.longpaths=true https://github.com/shinsengumi-dot/otakutv C:\otakutv
cd C:\otakutv
python -m venv tools\venv
tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt -r tools\requirements-photos.txt
```

## 3. Connect to the box

1. On the box: **Settings > Device Preferences > About**, press OK on **Build** seven times.
2. **Settings > Device Preferences > Developer options**: turn on **USB debugging** (on most boxes this also
   opens network debugging on port 5555).
3. On the computer:

```powershell
$env:TV_HOST = '192.168.1.50'      # your box's IP
tools\venv\Scripts\python.exe -I tools\tv.py --probe
```

The first time, the box asks to allow the computer: tick **Always allow** and confirm. `--probe` prints the model
and Android version.

## 4. Debloat

Back up first, then disable. The Mi Box 3 list is ready; for another model, pick its folder in `examples/`.

```powershell
tools\venv\Scripts\python.exe -I tools\tv.py "pm list packages -d" > backup-disabled-before.txt
powershell -ExecutionPolicy Bypass -File tools\disable.ps1 docs\otakutv\xiaomi-mi-box-3-debloat.txt
```

Open the list before running it: the voice assistant, Chromecast and the Google screensaver are kept, and the
lines marked *personal choice* can be commented out with `#`. Five minutes later, check that PatchWall stayed
off:

```powershell
tools\venv\Scripts\python.exe -I tools\tv.py "pm list packages -d | grep tvhome"
```

Optional privacy and speed tweaks (crash reports off, ad-blocking DNS, animations at 0.5×):

```powershell
powershell -ExecutionPolicy Bypass -File tools\otakutv-privacy.ps1 -TvHost $env:TV_HOST
```

## 5. Install the launcher

```powershell
powershell -ExecutionPolicy Bypass -File launcher\build-and-install.ps1
```

The script builds the launcher, installs it, makes it Home, and only then disables the stock launcher. Press
Home on the remote: the OtakuTV home screen appears.

## 6. Weather next to the clock (optional)

```powershell
cd addons\weather
.\gradlew.bat assembleRelease
cd ..\..
tools\venv\Scripts\python.exe -I tools\b64push.py addons\weather\app\build\outputs\apk\release\app-release.apk /data/local/tmp/addon.apk
tools\venv\Scripts\python.exe -I tools\tv.py "pm install -r /data/local/tmp/addon.apk; rm -f /data/local/tmp/addon.apk"
```

Press Home and answer **Show card**, open the **Weather** card, then type and pick your city. The weather
appears under the date once your theme has `"header_weather": true` (next step).

## 7. Your anime wallpapers

1. Copy the [example theme](theme-example/README.md) **outside** the repository, so your pictures are never committed:

   ```powershell
   Copy-Item -Recurse docs\otakutv\theme-example C:\otakutv-theme
   ```

2. Download the pictures you want **in your browser** (respect each site's terms: most allow personal use
   only), put them in a folder, then convert them for the TV:

   ```powershell
   tools\venv\Scripts\python.exe -I tools\wallpaper_prep.py $HOME\Pictures\Wallpaper C:\otakutv-theme\wallpapers --size 1920x1080 --brightness 0.6 --gradient 0.45
   ```

   The tool prints which source became `01.jpg`, `02.jpg`, and so on.

3. Name each picture in `C:\otakutv-theme\wallpapers\captions.txt`, one line per picture, a **Tab** between
   the file and the name, and ` | ` before the Japanese name:

   ```text
   01.jpg	Tanjiro Kamado | 竈門炭治郎
   02.jpg	Akaza | 猗窩座
   ```

4. Push the theme:

   ```powershell
   powershell -ExecutionPolicy Bypass -File tools\theme.ps1 -TvHost $env:TV_HOST C:\otakutv-theme
   ```

To add or remove pictures later, repeat steps 2 to 4. Change the speed with `wallpaper_rotation_seconds` and the
effect with `wallpaper_transition` (`slide`, `fade` or `none`) in `config.json`.

## 8. Close ADB

When you are done, turn **USB debugging** off in Developer options. On the Mi Box it also cuts power to the USB
port while it is on, and port 5555 lets anyone on your network control the box.

## Undo

| What | Command |
|---|---|
| Theme | `powershell -File tools\theme.ps1 -TvHost <ip> -Reset` |
| Privacy tweaks | `powershell -File tools\otakutv-privacy.ps1 -TvHost <ip> -Undo` |
| Debloat and Home | `powershell -File tools\restore.ps1 docs\otakutv\xiaomi-mi-box-3-debloat.txt`: re-enables the list, gives Home back to the stock launcher and restores the animation scales recorded by `disable.ps1` |
| Launcher | After `restore.ps1`, uninstall OtakuTV from Settings > Apps if you want it gone |

## Troubleshooting

- **`Filename too long` during the clone or a build**: the path is too deep. Clone to `C:\otakutv` with
  `core.longpaths=true`, as in step 2.
- **`--probe` hangs**: the box is asleep or debugging is off; wake it and check the IP.
- **No weather under the date**: open the Weather card once and pick a city; the line appears within a minute.
- **Wallpapers do not change**: `wallpaper_rotation_minutes` or `wallpaper_rotation_seconds` must be above 0, and
  the pictures must be in `wallpapers/` beside `config.json`.

More help: [docs/TROUBLESHOOTING.md](../TROUBLESHOOTING.md).
