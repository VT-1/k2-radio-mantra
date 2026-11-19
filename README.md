# K2 Radio Kiosk — Radio Mantra

**Пакет:** `com.al.k2mantra` · **minSdk 21 / targetSdk 34** · **Собрано:** 2026-11-19

> **AI-generated / Сгенерировано ИИ** — приложение написано, собрано и проверено
> ИИ-агентом (Hermes Agent) на реальном телевизоре Xiaomi MiTV. Ручных правок
> кода не было. / *The app was written, built and verified by an AI agent
> (Hermes Agent) on a real Xiaomi MiTV; no manual code edits.*

## Русский

Одноэкранное «киоск»-приложение для Android TV: название станции и одна большая
кнопка play/pause — чтобы радио включалось одним нажатием на пульте.

**Станция:** Radio Mantra, стрим `https://c22.radioboss.fm:8291/stream`.
До «В эфире» после запуска ~10 с (медленный старт стрима).

### Поведение
- автозапуск воспроизведения при открытии приложения;
- пауза **не** рвёт сеть (`MediaPlayer.pause()`) — возобновление мгновенное;
- автопереподключение при обрыве: до 8 попыток с интервалом 1.5 с;
- audio focus: если поверх запустилось другое аудио/видео — станция ставится на
  паузу; при возврате в приложение — возобновляется;
- нюанс Xiaomi TV: после потери фокуса и повторного запроса колбэк
  `AUDIOFOCUS_GAIN` может **не** доставиться — поэтому при возвращении в экран
  приложение принудительно запускает воспроизведение в `onResume`
  (проверено на железе 2026-11-19).

### UI и пульт
- тёмная тема, название станции (40 sp), статус: «Соединение…» / «В эфире» /
  «Пауза» / «Переподключение…» / «Ошибка — нажмите ещё раз» (интерфейс на русском);
- круглая кнопка 160 dp, подсветка акцентным цветом при фокусе;
- пульт: OK/ENTER на кнопке — play/pause.

### Установка
```bash
adb push dist/mantra-v1.apk /data/local/tmp/mantra.apk
adb shell pm install -r /data/local/tmp/mantra.apk
```

### Сборка
Тулчейн: build-tools r34 (aapt, d8, zipalign, apksigner) + `android.jar` API 34,
JDK 17. Цепочка:
`aapt package → javac → d8 (classes.dex в корень APK) → zipalign → apksigner`.
Кейстр: `build/ks.jks` (store/key password: `android`, alias: `k2`) — одна
подпись позволяет обновлять приложение без переустановки.
`resources.arsc` упаковывается без сжатия (требование API 30+).
Готовый APK: `dist/mantra-v1.apk` (подписан, выровнен, `apksigner verify` — чисто).

---

## English

A single-screen "kiosk" player for Android TV: the station name and one big
play/pause button, so the radio starts with a single remote press.

**Station:** Radio Mantra, stream `https://c22.radioboss.fm:8291/stream`.
Time to "on air" after launch is ~10 s (slow stream start).

### Behaviour
- starts playing automatically when launched;
- pause does **not** drop the network connection (`MediaPlayer.pause()`) —
  resuming is instant;
- auto-reconnect on stream drop: up to 8 retries, 1.5 s apart;
- audio focus: if another audio/video app takes focus, the station pauses; it
  resumes when this app comes back to the front;
- Xiaomi TV quirk: after losing audio focus and re-requesting it, the
  `AUDIOFOCUS_GAIN` callback may **not** be redelivered — so `onResume`
  force-starts playback when returning to the app (verified on device,
  2026-11-19).

### UI & remote
- dark theme; station title (40 sp); status line: "Соединение…" / "В эфире" /
  "Пауза" / "Переподключение…" / "Ошибка — нажмите ещё раз" (UI is in Russian);
- 160 dp round button, accent highlight on focus;
- remote: OK/ENTER on the button toggles play/pause.

### Install
```bash
adb push dist/mantra-v1.apk /data/local/tmp/mantra.apk
adb shell pm install -r /data/local/tmp/mantra.apk
```

### Build
Toolchain: build-tools r34 (aapt, d8, zipalign, apksigner) + `android.jar`
API 34, JDK 17. Pipeline:
`aapt package → javac → d8 (classes.dex at APK root) → zipalign → apksigner`.
Keystore: `build/ks.jks` (store/key password: `android`, alias: `k2`) — one
signing key lets you update the app without reinstalling.
`resources.arsc` is stored uncompressed (required for API 30+).
Prebuilt APK: `dist/mantra-v1.apk` (signed, aligned, `apksigner verify` clean).
