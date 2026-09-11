#!/usr/bin/env python3
"""Vitalis ML risk service (FastAPI).

Loads vitalis_model.pkl at startup and serves the endpoints the Android app calls:

    GET  /health   -> {"status": "ok"}
    POST /predict  -> {"risk_score": <0-100>}

The payload matches the Android app's RiskInputs (camelCase), prefixed with either
"row" (dict, field-based) or "features" (list, in training-column order):

    {"row": {"temperatureC": 33.0, "humidityPct": 60.0, ...}}

Run locally:  .venv/bin/uvicorn main:app --host 0.0.0.0 --port 5000
"""
import os
import re

import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

# Training columns, in the exact order used by train_model.py. "features" payloads
# must follow this order.
FEATURE_COLUMNS = [
    "temperature_c", "humidity_pct", "uv_index", "air_quality_aqi",
    "sleep_hours", "resting_heart_rate", "activity_minutes",
    "baseline_sleep_hours", "baseline_resting_heart_rate",
]

# Map the app's camelCase RiskInputs to the training column names.
_ALIASES = {
    "temperatureC": "temperature_c", "temperaturec": "temperature_c", "temperature": "temperature_c",
    "humidityPct": "humidity_pct", "humiditypct": "humidity_pct", "humidity": "humidity_pct",
    "uvIndex": "uv_index", "uvindex": "uv_index", "uv": "uv_index",
    "airQualityAqi": "air_quality_aqi", "airqualityaqi": "air_quality_aqi", "aqi": "air_quality_aqi",
    "sleepHours": "sleep_hours", "sleephours": "sleep_hours", "sleep": "sleep_hours",
    "restingHeartRate": "resting_heart_rate", "restingheartrate": "resting_heart_rate", "rhr": "resting_heart_rate",
    "activityMinutes": "activity_minutes", "activityminutes": "activity_minutes", "activity": "activity_minutes",
    "baselineSleepHours": "baseline_sleep_hours", "baselinesleephours": "baseline_sleep_hours",
    "baselineRestingHeartRate": "baseline_resting_heart_rate", "baselinerestingheartrate": "baseline_resting_heart_rate",
}


def _to_snake(name: str) -> str:
    s1 = re.sub(r"(.)([A-Z][a-z]+)", r"\1_\2", name)
    return re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", s1).lower()


def _build_row(row: dict) -> pd.DataFrame:
    data: dict = {}
    for key, value in row.items():
        col = _ALIASES.get(key) or _ALIASES.get(key.lower()) or _to_snake(key)
        if col in FEATURE_COLUMNS:
            data[col] = value
    if not data:
        raise HTTPException(status_code=400, detail="Payload row contains no recognized feature fields.")
    df = pd.DataFrame([data])
    return df.reindex(columns=FEATURE_COLUMNS).astype(object).infer_objects()


class PredictRequest(BaseModel):
    row: dict | None = None
    features: list | None = None


MODEL_PATH = os.getenv("MODEL_PATH", "vitalis_model.pkl")

app = FastAPI(title="Vitalis ML Risk Service", version="1.0")

if not os.path.exists(MODEL_PATH):
    raise RuntimeError(f"Model file not found: {MODEL_PATH}")
_model = joblib.load(MODEL_PATH)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/predict")
def predict(body: PredictRequest):
    if body.row is not None:
        df = _build_row(body.row)
    elif body.features is not None:
        df = pd.DataFrame([body.features], columns=FEATURE_COLUMNS)
    else:
        raise HTTPException(status_code=400, detail="Provide a 'row' object or a 'features' list.")

    # Never score a payload with zero usable inputs — median imputation would
    # otherwise return a confident mid-range guess for "no data at all".
    if int(df.notna().sum().sum()) == 0:
        raise HTTPException(status_code=422, detail="No usable inputs to score. Provide at least one feature.")

    pred = float(_model.predict(df)[0])
    if pred != pred:  # NaN
        raise HTTPException(status_code=422, detail="No usable inputs to score. Provide at least one feature.")
    return {"risk_score": round(min(max(pred, 0.0), 100.0), 1)}


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=int(os.getenv("PORT", 5000)))