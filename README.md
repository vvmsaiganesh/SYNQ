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

## Design

Dark "glass" aesthetic (see `ui/components/Glass.kt` + `ui/theme/Color.kt`): translucent
frosted panels with a bright gradient hairline border, ambient blurred colour orbs
drifting behind the content, a spring-animated risk ring, and shimmering skeleton tiles
while data loads. It's built entirely with alpha/gradient compositing (no
`RenderEffect`/backdrop blur), so it renders identically from API 26 up — no minimum
version cliff for the "glass" look itself.

`ui/theme/Type.kt` uses system serif/sans-serif as stand-ins for the prototype's
Fraunces/Public Sans webfonts. Drop matching `.ttf` files into `res/font/` and swap the
two `FontFamily` references in that file if you want pixel-parity with the web version.

## Permissions declared

- `INTERNET`, `ACCESS_NETWORK_STATE` — weather/geocoding calls.
- `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION` — "Use my location".
- `health.READ_STEPS`, `READ_SLEEP`, `READ_HEART_RATE`, `READ_RESTING_HEART_RATE`,
  `READ_EXERCISE`, `READ_HYDRATION`, `WRITE_HYDRATION` — Health Connect.

## Known follow-ups (left for you to decide on, not blockers)

- No app icon artwork beyond a simple vector placeholder — swap
  `res/drawable/ic_launcher_foreground.xml` for real branding whenever you have it.
- Bluetooth heart-rate-band pairing from the original web prototype (Web Bluetooth) was
  intentionally **not** ported — Health Connect's `RestingHeartRateRecord`/
  `HeartRateRecord` now cover that use case natively (any app that already writes to
   Health Connect, wearable OEM app included, feeds Synq automatically).
- `androidx.health.connect:connect-client:1.1.0` is current stable as of this build;
  check `https://developer.android.com/jetpack/androidx/releases/health-connect` before
  shipping in case a newer stable has landed.
