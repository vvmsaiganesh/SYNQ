# SYNQ

A native Android app, ported from the `SYNQ` prototype, that estimates
heat/sun outdoor-activity risk from live weather plus your own **Health Connect** data
(steps, sleep, resting heart rate, exercise, hydration). Dark, glass-panel UI throughout.

## Open it

1. Open this folder (`Synq/`) directly in **Android Studio (Koala/2024.1 or newer)**.
   Let it sync — it will fetch the Gradle wrapper JAR automatically on first sync (the
   wrapper *properties* file is included; Android Studio regenerates the binary wrapper
   itself, or you can run `gradle wrapper` once if you have a local Gradle install).
2. Run on a device or emulator with **API 26+** (Health Connect UI needs a real device
   or an emulator image with Play Store, since it depends on the Health Connect app).

## What's wired up

- **Health Connect** (`data/health/HealthConnectManager.kt`) — reads today's steps,
  exercise-session minutes, last night's sleep, resting heart rate (falls back to an
  average of raw heart-rate samples if no explicit resting-HR record exists), and
  today's logged hydration. Also **writes** hydration entries back to Health Connect
  when you tap `+250 ml` / `+500 ml` — so it round-trips with whatever app you actually
  log water in.
  - Permission request goes through the real system Health Connect permission sheet
    (`PermissionController.createRequestPermissionResultContract()`), launched from
    `MainActivity`. That sheet **is** the "login" — there's no separate account system.
  - If Health Connect isn't installed/supported on the test device, the app offers a
    **"Use demo data"** fallback (simulated steps/sleep/HR/hydration) so you can still
    exercise the full risk-scoring UI.
- **Weather** (`data/weather/`) — same three Open-Meteo endpoints (forecast, air
  quality, geocoding) and the BigDataCloud reverse-geocode call the HTML prototype
  used, called via Retrofit with absolute URLs so the requests are byte-for-byte
  equivalent to the browser version.
- **Risk engine** (`data/risk/RiskEngine.kt`) — a direct line-for-line Kotlin port of
  the scoring thresholds/weights embedded in `outdoor-guard.html`, so the same inputs
  produce the same score on both.
- **Location** — `FusedLocationProviderClient` (Play Services), requested at runtime.


## Permissions declared
