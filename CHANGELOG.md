# Changelog

OtakuTV follows [Semantic Versioning](https://semver.org/). The launcher's `versionName` carries the OtakuTV
version (`0.1.0-otakutv`); it is based on Clean Your TV launcher 3.5.

## [0.1.0] - 2026-11-18

First release, tested on a Xiaomi Mi Box 3 (MIBOX3, Android 9, firmware PI.3934).

### Added

- **Launcher**
  - `wallpaper_rotation_seconds` (5 to 86400): rotate wallpapers every few seconds. Overrides
    `wallpaper_rotation_minutes` when set; the wallpaper player keeps its own timer while Home is visible.
  - `wallpaper_transition`: `fade` (900 ms crossfade) or `slide` (800 ms sideways slide) between rotated
    pictures; `none` is the default and keeps the old instant change. Measured on the Mi Box 3 at 6 s with
    `slide`: 0 janky frames in 24 s.
  - Two-line captions: a `captions.txt` name written `Latin | 日本語` shows the second part on a smaller,
    softer line under the first.
  - `header_weather`: the Weather add-on's current reading (icon, temperature, sky) under the date, read every
    minute from the add-on's provider. Home still has no network permission.
  - Inter as the interface font (SIL OFL 1.1).
- **Weather add-on**: a periodic job (30 minutes, network required) keeps the current reading fresh, and a
  read-only provider (`io.github.sumyabazar1.weather.header`) hands it to the launcher.
- **Mi Box 3**: a tested debloat list (`docs/otakutv/xiaomi-mi-box-3-debloat.txt`) and the network and memory
  audit behind it (`docs/otakutv/AUDIT-mibox3.md`).
- `tools/otakutv-privacy.ps1`: crash reports to Google off, AdGuard DNS as Private DNS, system animations at
  0.5×, with `-Undo`.
- Documentation: README in English and French, install guide in both languages, example theme.

### Changed

- Launcher `versionCode` 15, `versionName` `0.1.0-otakutv`. It installs over Clean Your TV 3.5 built with the
  same debug key and keeps its settings.

### Known limits

- The launcher APK is about 994 KB with Inter, above Clean Your TV's 576 KB size gate.
- The application id is still `com.gathin.tvhome`, so OtakuTV replaces Clean Your TV's launcher rather than
  installing beside it.
- The ad-blocking DNS blocks ad networks but not app analytics such as Firebase Crashlytics.
