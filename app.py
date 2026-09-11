#!/usr/bin/env python3
"""Vitalis ML risk service.

Loads vitalis_model.pkl at startup and serves two endpoints the Android app calls:

    GET  /health   -> {"status": "ok"}
    POST /predict  -> {"risk_score": <0-100>}

The payload is a JSON object matching the Android app's RiskInputs (camelCase),
prefixed with either "row" (dict, field-based) or "features" (list, in the same
order as the training columns):

    {"row": {"temperatureC": 33.0, "humidityPct": 60.0, ...}}

Run:  .venv/bin/python app.py   (serves on 0.0.0.0:5000)
"""
import os
import re

import numpy as np
import pandas as pd
from flask import Flask, jsonify, request
import joblib

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
        raise ValueError("Payload row contains no recognized feature fields.")
    df = pd.DataFrame([data])
    # Align to the training schema so the pipeline sees every column.
    df = df.reindex(columns=FEATURE_COLUMNS)
    return df.astype(object).infer_objects()


MODEL_PATH = os.getenv("MODEL_PATH", "vitalis_model.pkl")

app = Flask(__name__)

_model = None


def _load_model():
    global _model
    if _model is None:
        if not os.path.exists(MODEL_PATH):
            raise RuntimeError(f"Model file not found: {MODEL_PATH}")
        _model = joblib.load(MODEL_PATH)
    return _model


@app.route("/health")
def health():
    return jsonify({"status": "ok"})


@app.route("/predict", methods=["POST"])
def predict():
    try:
        payload = request.get_json(force=True)
        if "row" in payload:
            df = _build_row(payload["row"])
        elif "features" in payload:
            features = payload["features"]
            df = pd.DataFrame([features], columns=FEATURE_COLUMNS)
        else:
            return jsonify({"error": "Provide a 'row' object or a 'features' list."}), 400

        model = _load_model()
        # Never score a payload with zero usable inputs — median imputation would
        # otherwise return a confident mid-range guess for "no data at all".
        if int(df.notna().sum().sum()) == 0:
            return jsonify({"error": "No usable inputs to score. Provide at least one feature."}), 422
        pred = float(model.predict(df)[0])
        if pred != pred:
            return jsonify({"error": "No usable inputs to score. Provide at least one feature."}), 422
        return jsonify({"risk_score": round(min(max(pred, 0.0), 100.0), 1)})
    except Exception as e:
        app.logger.exception("predict failed")
        return jsonify({"error": str(e)}), 500


if __name__ == "__main__":
    _load_model()
    port = int(os.getenv("PORT", 5000))
    app.run(host="0.0.0.0", port=port, debug=False)