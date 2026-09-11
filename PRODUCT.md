# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

General health-conscious users who spend time outdoors and want personalized heat/sun risk awareness before heading out. No specific medical or athletic audience — anyone who wants to know "should I be careful today?"

## Product Purpose

Synq estimates heat and sun outdoor-activity risk by combining live environmental data (temperature, humidity, UV index, air quality) with the user's own health data from Health Connect (sleep, resting heart rate, activity, hydration). It gives a single risk score with context-specific recommendations, helping users make informed decisions about outdoor activity.

## Positioning

The differentiator is personalization: most heat-risk tools show generic weather warnings. Synq factors in the user's actual physiological state — poor sleep, elevated heart rate, recent exertion — to produce a risk score that reflects their individual vulnerability, not just the weather. The Health Connect integration means it reads data the user's other apps already collect, requiring no extra effort.

## Operating Context

- Single-screen app: the user opens it, sees their risk score, conditions, and wearable data in one scrollable view.
- Health Connect is the "login" — the system permission sheet is the only onboarding step. No account system.
- Location is requested at launch; city search is the fallback.
- Hydration logging writes back to Health Connect, syncing with whatever other water-tracking app the user has.
- Personal baseline comparison (sleep, RHR) lets users calibrate against their own norms rather than population averages.

## Capabilities and Constraints

- **Weather data:** Open-Meteo API (forecast, air quality, geocoding) + BigDataCloud reverse geocoding. No API key required.
- **Health data:** Health Connect SDK (read steps, sleep, heart rate, resting heart rate, exercise, hydration; write hydration). Requires Health Connect app on device (API 26+).
- **Location:** FusedLocationProviderClient (Play Services), runtime permission.
- **Risk engine:** Weighted scoring across 7 factors (temperature 25%, humidity 15%, UV 15%, air quality 10%, sleep 15%, RHR 10%, activity 10%). Ported line-for-line from the original HTML prototype.
- **Demo mode:** Simulated health data when Health Connect is unavailable, so the full UI is exercisable without a real device.
- **No Bluetooth HR pairing** — intentionally not ported from the web prototype; Health Connect covers this natively.
- **No app icon** beyond a vector placeholder.
- **Min API 26** — the glass aesthetic uses alpha/gradient compositing only (no RenderEffect/backdrop blur), so it renders identically across all supported versions.
- **Stack:** Kotlin, Jetpack Compose, Material3, Retrofit, Health Connect SDK 1.1.0. Single-activity architecture.

## Brand Commitments

- Name: **Synq**
- Tagline: "Know before you go"
- Voice: Calm, direct, non-alarming. Avoids medical language. Informs without scaring.
- Visual identity: Dark glass aesthetic (frosted translucent panels on near-black canvas, ambient glow orbs, gradient hairline borders). Warm sand/clay accents. Serif display font + sans-serif body (system stand-ins for Fraunces/Public Sans).

## Evidence on Hand

- Complete working codebase: single-screen app with all features wired.
- README with architecture docs, permission list, and known follow-ups.
- Risk engine validated against the original HTML prototype (same inputs produce same scores).
- No real user research, testimonials, or content assets — the app generates all copy dynamically from risk inputs.

## Product Principles

1. **Data over opinion.** The risk score is derived from the user's actual conditions and health state, not generic advice.
2. **Nothing leaves the device.** Health data stays on-device via Health Connect; weather data comes from public APIs with no user accounts.
3. **Graceful degradation.** The app works with partial data — missing health signals reduce coverage but don't break the experience. Demo mode covers devices without Health Connect.
4. **One glance, one answer.** The hero risk ring and summary should be comprehensible in under 3 seconds, with detail available on scroll.
