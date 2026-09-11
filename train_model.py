#!/usr/bin/env python3
import os
import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_absolute_error, r2_score

DATA_PATH = os.getenv("DATA_PATH", "data/training_data.csv")
MODEL_OUTPUT = os.getenv("MODEL_OUTPUT", "vitalis_model.pkl")
RANDOM_STATE = 42

if not os.path.exists(DATA_PATH):
    raise FileNotFoundError(f"Training data not found: {DATA_PATH}")

df = pd.read_csv(DATA_PATH)
if "risk" not in df.columns:
    raise ValueError("Training data must contain a 'risk' column.")

X = df.drop(columns=["risk"])
y = df["risk"]

numeric_features = X.select_dtypes(include=["number"]).columns.tolist()
categorical_features = X.select_dtypes(include=["object", "category"]).columns.tolist()

numeric_transformer = Pipeline(steps=[
    ("imputer", SimpleImputer(strategy="median")),
    ("scaler", StandardScaler())
])

categorical_transformer = Pipeline(steps=[
    ("imputer", SimpleImputer(strategy="most_frequent")),
    ("onehot", OneHotEncoder(handle_unknown="ignore"))
])

preprocessor = ColumnTransformer(
    transformers=[
        ("num", numeric_transformer, numeric_features),
        ("cat", categorical_transformer, categorical_features)
    ]
)

model = RandomForestRegressor(n_estimators=250, random_state=RANDOM_STATE, n_jobs=-1)

pipeline = Pipeline(steps=[("preprocessor", preprocessor), ("model", model)])

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=RANDOM_STATE)
pipeline.fit(X_train, y_train)

y_pred = pipeline.predict(X_test)
print("MAE:", round(mean_absolute_error(y_test, y_pred), 3))
print("R2:", round(r2_score(y_test, y_pred), 3))

# ---- Anti-hallucination probes: hand-picked corner/classic scenarios must land in
# the "correct" band. If any probe is wildly off, the model is extrapolating.
PROBES = [
    ("heat wave + no sleep", {"temperature_c": 41, "humidity_pct": 96, "uv_index": 12,
                              "air_quality_aqi": 320, "sleep_hours": 0.3,
                              "resting_heart_rate": 120, "activity_minutes": 210,
                              "baseline_sleep_hours": 7.3, "baseline_resting_heart_rate": 62}, (80, 100)),
    ("perfect day", {"temperature_c": 20, "humidity_pct": 42, "uv_index": 2, "air_quality_aqi": 25,
                     "sleep_hours": 8.5, "resting_heart_rate": 58, "activity_minutes": 5,
                     "baseline_sleep_hours": 7.3, "baseline_resting_heart_rate": 62}, (0, 30)),
    ("3h sleep, mild weather", {"temperature_c": 21, "humidity_pct": 45, "uv_index": 2,
                                "air_quality_aqi": 30, "sleep_hours": 3, "resting_heart_rate": 62,
                                "activity_minutes": 10, "baseline_sleep_hours": 7.3,
                                "baseline_resting_heart_rate": 62}, (60, 100)),
    ("no sleep data, mild weather", {"temperature_c": 25, "humidity_pct": 60, "uv_index": 5,
                                     "air_quality_aqi": 60, "sleep_hours": None,
                                     "resting_heart_rate": None, "activity_minutes": None,
                                     "baseline_sleep_hours": None, "baseline_resting_heart_rate": None}, (0, 40)),
    ("cold day, ok health", {"temperature_c": -2, "humidity_pct": 50, "uv_index": 0,
                             "air_quality_aqi": 40, "sleep_hours": 7.5, "resting_heart_rate": 62,
                             "activity_minutes": 15, "baseline_sleep_hours": 7.3,
                             "baseline_resting_heart_rate": 62}, (0, 30)),
]
print("\nAnti-hallucination probes (expected band -> predicted):")
for label, row, band in PROBES:
    pred = float(pipeline.predict(pd.DataFrame([row]))[0])
    ok = "OK " if band[0] <= pred <= band[1] else "FAIL"
    print(f"  [{ok}] {label:28s} expected {band} -> {pred:.1f}")

joblib.dump(pipeline, MODEL_OUTPUT)
print(f"\nModel saved to {MODEL_OUTPUT}")