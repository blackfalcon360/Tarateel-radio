# Tarateel Radio (Android)

Mp3Quran Tarateel live radio ke liye simple Android app.

- Stream: `https://qurango.net/radio/tarateel` (128 kbps MP3)
- Kotlin + AndroidX Media3 (ExoPlayer + MediaSession)
- Background playback, notification controls, lock-screen controls
- minSdk 24 (Android 7.0+)

## APK kaise banayein (GitHub par)
1. Is folder ko GitHub repo mein push karein.
2. **Actions** tab -> **Build APK** -> run hone dein (ya "Run workflow").
3. Workflow khatam hone par **Artifacts** se `TarateelRadio-debug-apk` download karein.

## Android Studio se
Folder open karein -> Run. (Android Studio khud Gradle wrapper bana lega.)

## Stream URL badalna
`app/src/main/java/com/blackradio/tarateel/MainActivity.kt` mein `STREAM_URL` badal dein.
