# Installer OtakuTV

[English](INSTALL.md) · [Retour au README](../../README.fr.md)

Comptez environ une heure la première fois, surtout de l'attente pendant les compilations. Chaque étape est
réversible ; la dernière section explique comment. Les commandes sont pour PowerShell sous Windows ; sous macOS
et Linux, utilisez le script `.sh` équivalent et `tools/venv/bin/python` au lieu de `tools\venv\Scripts\python.exe`.

## 1. Ce qu'il faut

- **La box** : Android TV 8 ou plus récent. Notez son adresse IP (Paramètres > Réseau).
- **Un ordinateur** sur le même réseau, avec :
  - [Git](https://git-scm.com/) et [Python 3.10 ou plus](https://www.python.org/) ;
  - **Java 17 (JDK)** et le **SDK Android** (plateforme 34, build-tools 34.0.0). Le plus simple est
    d'installer [Android Studio](https://developer.android.com/studio) ; la compilation télécharge seule les
    éléments manquants du SDK une fois les licences acceptées.

## 2. Récupérer le code (dans un chemin court)

Windows limite les chemins à 260 caractères, et ce dépôt a des dossiers profonds. Clonez-le près de la racine
du disque :

```powershell
git clone -c core.longpaths=true https://github.com/shinsengumi-dot/otakutv C:\otakutv
cd C:\otakutv
python -m venv tools\venv
tools\venv\Scripts\python.exe -m pip install -r tools\requirements.txt -r tools\requirements-photos.txt
```

## 3. Se connecter à la box

1. Sur la box : **Paramètres > Préférences de l'appareil > À propos**, appuyez 7 fois sur OK sur **Build**.
2. **Paramètres > Préférences de l'appareil > Options pour les développeurs** : activez le **débogage USB**
   (sur la plupart des box, il ouvre aussi le débogage réseau sur le port 5555).
3. Sur l'ordinateur :

```powershell
$env:TV_HOST = '192.168.1.50'      # l'adresse IP de votre box
tools\venv\Scripts\python.exe -I tools\tv.py --probe
```

La première fois, la box demande d'autoriser l'ordinateur : cochez **Toujours autoriser** et validez. `--probe`
affiche le modèle et la version d'Android.

## 4. Débloat

Sauvegardez d'abord, puis désactivez. La liste Mi Box 3 est prête ; pour un autre modèle, prenez son dossier
dans `examples/`.

```powershell
tools\venv\Scripts\python.exe -I tools\tv.py "pm list packages -d" > backup-desactives-avant.txt
powershell -ExecutionPolicy Bypass -File tools\disable.ps1 docs\otakutv\xiaomi-mi-box-3-debloat.txt
```

Ouvrez la liste avant de la lancer : l'Assistant vocal, Chromecast et l'écran de veille Google sont gardés, et
les lignes marquées *personal choice* peuvent être neutralisées avec un `#`. Cinq minutes après, vérifiez que
PatchWall est resté désactivé :

```powershell
tools\venv\Scripts\python.exe -I tools\tv.py "pm list packages -d | grep tvhome"
```

Réglages optionnels de confidentialité et de vitesse (rapports d'erreurs coupés, DNS anti-pub, animations à
0,5×) :

```powershell
powershell -ExecutionPolicy Bypass -File tools\otakutv-privacy.ps1 -TvHost $env:TV_HOST
```

## 5. Installer le launcher

```powershell
powershell -ExecutionPolicy Bypass -File launcher\build-and-install.ps1
```

Le script compile le launcher, l'installe, en fait l'écran d'accueil, et seulement ensuite désactive le launcher
d'origine. Appuyez sur Home sur la télécommande : l'accueil OtakuTV apparaît.

## 6. La météo à côté de l'heure (optionnel)

```powershell
cd addons\weather
.\gradlew.bat assembleRelease
cd ..\..
tools\venv\Scripts\python.exe -I tools\b64push.py addons\weather\app\build\outputs\apk\release\app-release.apk /data/local/tmp/addon.apk
tools\venv\Scripts\python.exe -I tools\tv.py "pm install -r /data/local/tmp/addon.apk; rm -f /data/local/tmp/addon.apk"
```

Appuyez sur Home et répondez **Show card**, ouvrez la carte **Weather**, puis tapez et choisissez votre ville.
La météo apparaît sous la date dès que votre thème contient `"header_weather": true` (étape suivante).

## 7. Vos fonds d'écran anime

1. Copiez le [thème d'exemple](theme-example/README.md) **en dehors** du dépôt, pour que vos images ne soient jamais publiées :

   ```powershell
   Copy-Item -Recurse docs\otakutv\theme-example C:\otakutv-theme
   ```

2. Téléchargez les images voulues **dans votre navigateur** (respectez les conditions de chaque site : la
   plupart n'autorisent que l'usage personnel), mettez-les dans un dossier, puis convertissez-les pour la TV :

   ```powershell
   tools\venv\Scripts\python.exe -I tools\wallpaper_prep.py $HOME\Pictures\Wallpaper C:\otakutv-theme\wallpapers --size 1920x1080 --brightness 0.6 --gradient 0.45
   ```

   L'outil affiche quelle image d'origine est devenue `01.jpg`, `02.jpg`, etc.

3. Nommez chaque image dans `C:\otakutv-theme\wallpapers\captions.txt` : une ligne par image, une
   **tabulation** entre le fichier et le nom, et ` | ` avant le nom japonais :

   ```text
   01.jpg	Tanjiro Kamado | 竈門炭治郎
   02.jpg	Akaza | 猗窩座
   ```

4. Envoyez le thème :

   ```powershell
   powershell -ExecutionPolicy Bypass -File tools\theme.ps1 -TvHost $env:TV_HOST C:\otakutv-theme
   ```

Pour ajouter ou retirer des images plus tard, refaites les étapes 2 à 4. Changez la vitesse avec
`wallpaper_rotation_seconds` et l'effet avec `wallpaper_transition` (`slide`, `fade` ou `none`) dans
`config.json`.

## 8. Fermer ADB

Quand vous avez terminé, désactivez le **débogage USB** dans les Options pour les développeurs. Sur la Mi Box,
tant qu'il est actif, il coupe aussi l'alimentation du port USB, et le port 5555 permet à n'importe qui sur
votre réseau de contrôler la box.

## Tout annuler

| Quoi | Commande |
|---|---|
| Thème | `powershell -File tools\theme.ps1 -TvHost <ip> -Reset` |
| Réglages de confidentialité | `powershell -File tools\otakutv-privacy.ps1 -TvHost <ip> -Undo` |
| Débloat et accueil | `powershell -File tools\restore.ps1 docs\otakutv\xiaomi-mi-box-3-debloat.txt` : réactive la liste, rend l'accueil au launcher d'origine et remet les vitesses d'animation notées par `disable.ps1` |
| Launcher | Après `restore.ps1`, désinstallez OtakuTV depuis Paramètres > Applis si vous voulez le retirer |

## Dépannage

- **`Filename too long` pendant le clonage ou la compilation** : le chemin est trop profond. Clonez dans
  `C:\otakutv` avec `core.longpaths=true`, comme à l'étape 2.
- **`--probe` ne répond pas** : la box est en veille ou le débogage est coupé ; réveillez-la et vérifiez l'IP.
- **Pas de météo sous la date** : ouvrez une fois la carte Weather et choisissez une ville ; la ligne apparaît
  dans la minute.
- **Les fonds d'écran ne changent pas** : `wallpaper_rotation_minutes` ou `wallpaper_rotation_seconds` doit être
  supérieur à 0, et les images doivent être dans `wallpapers/` à côté de `config.json`.

Plus d'aide : [docs/TROUBLESHOOTING.md](../TROUBLESHOOTING.md) (en anglais).
