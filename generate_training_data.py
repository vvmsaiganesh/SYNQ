#!/usr/bin/env python3
"""Generates realistic training data for the Vitalis ML risk model.

The target column (risk) is computed with a Python port of the current Kotlin
RiskEngine (same thresholds, weights and max-of-signals logic), plus a little
Gaussian noise so the RandomForest learns the pattern instead of memorizing it.
Run:  python3 generate_training_data.py
"""
import csv
import os
import random

random.seed(42)


def score_temperature(t):
    if t is None: return None
    if t < 27: return 5
    if t < 32: return 30
    if t < 35: return 55
    if t < 38: return 75
    if t < 41: return 90
    return 100


def score_humidity(h):
    if h is None: return None
    if h < 40: return 5
    if h < 55: return 20
    if h < 70: return 45
    if h < 85: return 70
    return 90


def score_uv(u):
    if u is None: return None
    if u < 3: return 5
    if u < 6: return 30
    if u < 8: return 55
    if u < 11: return 80
    return 100


def score_aqi(a):
    if a is None: return None
    if a <= 50: return 5
    if a <= 100: return 25
    if a <= 150: return 50
    if a <= 200: return 75
    if a <= 300: return 90
    return 100


def sleep_risk_target(sleep):
    if sleep < 1.5: return 95
    if sleep < 2.5: return 90
    if sleep < 3.5: return 80
    if sleep < 4.5: return 75
    if sleep < 5.5: return 35
    if sleep < 6.5: return 28
    return 22


def score_sleep(sleep, baseline):
    if sleep is None: return None
    if baseline is not None:
        d = baseline - sleep
        if d <= 0: return 10
        if d < 1: return 40
        if d < 2: return 75
        if d < 3: return 90
        return 95
    if sleep >= 7: return 10
    if sleep >= 6: return 28
    if sleep >= 5: return 35
    if sleep >= 4: return 75
    if sleep >= 3: return 80
    if sleep >= 2: return 90
    if sleep >= 1: return 95
    return 100


def score_rhr(rhr, baseline):
    if rhr is None: return None
    if baseline is not None:
        d = rhr - baseline
        if d <= 2: return 10
        if d <= 6: return 35
        if d <= 10: return 60
        if d <= 15: return 80
        return 95
    if rhr < 65: return 10
    if rhr < 75: return 30
    if rhr < 85: return 55
    if rhr < 95: return 75
    return 90


def score_activity(mins):
    if mins is None: return None
    if mins < 15: return 10
    if mins < 30: return 25
    if mins < 60: return 45
    if mins < 90: return 65
    return 85


ENV_WEIGHTS = {"temperature": 20, "humidity": 10, "uv": 10, "aqi": 5}


def compute_risk(row):
    scores = {
        "temperature": score_temperature(row["temperature_c"]),
        "humidity": score_humidity(row["humidity_pct"]),
        "uv": score_uv(row["uv_index"]),
        "aqi": score_aqi(row["air_quality_aqi"]),
        "sleep": score_sleep(row["sleep_hours"], row["baseline_sleep_hours"]),
        "rhr": score_rhr(row["resting_heart_rate"], row["baseline_resting_heart_rate"]),
        "activity": score_activity(row["activity_minutes"]),
    }
    present = {k: v for k, v in scores.items() if v is not None}

    env_present = {k: v for k, v in present.items() if k in ENV_WEIGHTS}
    env_risk = 0
    if env_present:
        total = sum(ENV_WEIGHTS[k] for k in env_present)
        s = sum(ENV_WEIGHTS[k] * v for k, v in env_present.items())
        env_risk = s / total

    sleep_floor = sleep_risk_target(row["sleep_hours"]) if row["sleep_hours"] is not None else 0
    rhr_risk = present.get("rhr", 0)
    activity_risk = present.get("activity", 0)

    risk = max(sleep_floor, env_risk, rhr_risk, activity_risk)
    risk = min(risk, 100.0) + random.gauss(0, 1.2)
    return round(min(max(risk, 0.0), 100.0), 1)


def sample_row():
    def maybe_fill(value, missing_rate=0.08):
        return value if random.random() > missing_rate else None  # ~8% missing per factor

    def gauss(mu, sigma, lo, hi):
        return min(max(random.gauss(mu, sigma), lo), hi)

    # 85% "typical day" (bell-shaped) + 15% uniform tail that reaches the extreme
    # corners real devices can report (sleep <1h, heat wave, RHR >100, ...). Without
    # the tail the model hallucinates on out-of-distribution inputs.
    def mix(mu, sigma, lo, hi):
        if random.random() < 0.15:
            return random.uniform(lo, hi)
        return gauss(mu, sigma, lo, hi)

    baseline_sleep = gauss(7.3, 0.7, 5, 9)
    baseline_rhr = random.randint(55, 75)
    return {
        "temperature_c": maybe_fill(round(mix(24, 7, 0, 43), 1)),
        "humidity_pct": maybe_fill(round(mix(55, 20, 15, 100), 1)),
        "uv_index": maybe_fill(round(mix(5, 2.5, 0, 12), 1)),
        "air_quality_aqi": maybe_fill(round(mix(60, 40, 10, 400), 0)),
        "sleep_hours": maybe_fill(round(mix(7, 1.5, 0.2, 9), 1)),
        "resting_heart_rate": maybe_fill(round(mix(68, 10, 45, 130), 0)),
        "activity_minutes": maybe_fill(round(mix(40, 30, 0, 240), 0)),
        "baseline_sleep_hours": baseline_sleep if maybe_fill(0.0) is not None else None,
        "baseline_resting_heart_rate": baseline_rhr if maybe_fill(0.0) is not None else None,
    }


# Expert-picked landmarks that pin down the behavior at corners and classics so the
# model can't drift. These are written with the engine's exact score (no noise).
ANCHORS = [
    # All worst case
    {"temperature_c": 41, "humidity_pct": 96, "uv_index": 12, "air_quality_aqi": 320,
     "sleep_hours": 0.3, "resting_heart_rate": 120, "activity_minutes": 210},
    # Heat wave, otherwise healthy
    {"temperature_c": 40, "humidity_pct": 80, "uv_index": 11, "air_quality_aqi": 110,
     "sleep_hours": 8, "resting_heart_rate": 60, "activity_minutes": 10},
    # Perfect conditions
    {"temperature_c": 20, "humidity_pct": 42, "uv_index": 2, "air_quality_aqi": 25,
     "sleep_hours": 8.5, "resting_heart_rate": 58, "activity_minutes": 5},
    # Dangerously little sleep, otherwise perfect weather
    {"temperature_c": 22, "humidity_pct": 45, "uv_index": 3, "air_quality_aqi": 30,
     "sleep_hours": 0.4, "resting_heart_rate": 62, "activity_minutes": 0},
    # No sleep data at all, weather only
    {"temperature_c": 25, "humidity_pct": 60, "uv_index": 5, "air_quality_aqi": 60,
     "sleep_hours": None, "resting_heart_rate": None, "activity_minutes": None},
    # Freezing-cold day, sleep was short
    {"temperature_c": -2, "humidity_pct": 50, "uv_index": 0, "air_quality_aqi": 40,
     "sleep_hours": 4.5, "resting_heart_rate": 70, "activity_minutes": 20},
    # Everything missing but RHR (baseline-comparison high)
    {"temperature_c": None, "humidity_pct": None, "uv_index": None, "air_quality_aqi": None,
     "sleep_hours": None, "resting_heart_rate": 118, "activity_minutes": None},
    # Everything missing but activity
    {"temperature_c": None, "humidity_pct": None, "uv_index": None, "air_quality_aqi": None,
     "sleep_hours": None, "resting_heart_rate": None, "activity_minutes": 210},
    # High humidity + high temp, good health
    {"temperature_c": 34, "humidity_pct": 92, "uv_index": 6, "air_quality_aqi": 70,
     "sleep_hours": 8.2, "resting_heart_rate": 59, "activity_minutes": 15},
    # Bad air quality only
    {"temperature_c": 22, "humidity_pct": 45, "uv_index": 3, "air_quality_aqi": 340,
     "sleep_hours": 8, "resting_heart_rate": 60, "activity_minutes": 10},
]


COLUMNS = [
    "temperature_c", "humidity_pct", "uv_index", "air_quality_aqi",
    "sleep_hours", "resting_heart_rate", "activity_minutes",
    "baseline_sleep_hours", "baseline_resting_heart_rate", "risk",
]


def main():
    n_rows = int(os.getenv("N_ROWS", "4000"))
    out_dir = "data"
    os.makedirs(out_dir, exist_ok=True)
    path = os.path.join(out_dir, "training_data.csv")

    with open(path, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=COLUMNS)
        writer.writeheader()
        for _ in range(n_rows):
            row = sample_row()
            row["risk"] = compute_risk(row)
            writer.writerow(row)
        # Anchor rows use the engine's exact score (no noise) to pin corner behavior.
        for row in ANCHORS:
            row = {"baseline_sleep_hours": 7.3, "baseline_resting_heart_rate": 62.0, **row}
            row["risk"] = compute_risk(row)
            writer.writerow(row)

    print(f"Wrote {n_rows} rows + {len(ANCHORS)} anchor rows -> {path}")


if __name__ == "__main__":
    main()