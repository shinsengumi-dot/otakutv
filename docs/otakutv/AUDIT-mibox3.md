# Audit debloat — Mi Box 3 (MIBOX3, Android 9, PI.3934)

Mesuré le 2026-11-18, box allumée depuis 23 h 24. Les compteurs réseau sont cumulés depuis le démarrage :
ils incluent donc la période d'avant le débloat du 17/11.

## Réseau par appli (reçu / envoyé depuis le démarrage)

| Appli | Reçu | Envoyé | Lecture |
|---|---|---|---|
| Crunchyroll | 1 976 Mo | 54 Mo | Usage normal (streaming) |
| YouTube | 1 512 Mo | 26 Mo | Usage normal |
| Backdrop (écran de veille Chromecast) | 194 Mo | 7 Mo | Télécharge en continu des photos Google ; gardé par choix |
| PatchWall | 34 Mo | 2,5 Mo | Avant sa désactivation ; 0 depuis |
| **Prime Video** | 17 Mo | **5 Mo** | **Trafic en arrière-plan sans être utilisé** |
| Play Store | 16 Mo | 1 Mo | Mises à jour automatiques |
| Services Google (gms) | 6 Mo | 2,3 Mo | Nécessaire |
| Chromecast (mediashell) | 5,7 Mo | **6,2 Mo** | Envoie plus qu'il ne reçoit ; gardé par choix |
| **Netflix** | 4,4 Mo | 1,6 Mo | Arrière-plan |
| Assistant vocal (katniss) | 3 Mo | 1,2 Mo | Gardé par choix |

## Mémoire (PSS) des processus qui ne servent pas à ce que vous regardez

| Processus | RAM | Lecture |
|---|---|---|
| Assistant vocal (`katniss:interactor`) | 82 Mo | Gardé par choix |
| Backdrop | 65 Mo | Gardé par choix ; un écran de veille local à la place libérerait 65 Mo |
| Play Store (2 processus) | 107 Mo | Tourne même sans être ouvert |
| Synthèse vocale Google (TTS) | 41 Mo | Sert aux réponses parlées de l'Assistant |
| Chromecast | 26 Mo | Gardé par choix |
| Play Jeux | — | Fait tourner un service de recommandations sans être utilisé |

## Ce que le dépôt original ne traite pas

1. **Rapports d'erreurs envoyés à Google** : `send_action_app_error=1`. Réversible : `settings put secure send_action_app_error 0`.
2. **Synchronisation agenda et contacts** (`syncadapters.calendar`, `syncadapters.contacts`) : inutile sur une box,
   et le fournisseur d'agenda réveille la box (4 réveils mesurés).
3. **Télémétrie dans les applis elles-mêmes** : Crunchyroll et Spotify font tourner `firebase.sessions`.
   On ne peut pas la désactiver sans casser l'appli, mais un **DNS anti-pistage** (Private DNS, par exemple
   AdGuard DNS) la bloque pour toutes les applis d'un coup. Non configuré sur la box (`private_dns_mode=null`).
4. **Applis préinstallées actives en arrière-plan** : Prime Video, Netflix et Play Jeux consomment du réseau ou
   de la mémoire sans être utilisées.
5. **Réactivité** : les animations système sont à 1× ; à 0,5×, les menus Android s'ouvrent deux fois plus vite.
   Réversible.
6. **Mises à jour automatiques du Play Store** : à couper depuis le Play Store (pas faisable par ADB).

## À ne pas toucher (confirmé par la mesure)

`mitv.service` (sondage TV), `com.droidlogic` (système Amlogic, Bluetooth, NTP),
`partnercustomizer` (boutons de la télécommande), Play Store et services Google.
