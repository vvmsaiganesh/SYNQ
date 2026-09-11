# Project: SYNQ (Android outdoor risk app)

## Status: ML upgrade in progress (hackathon)

The hardcoded `RiskEngine.kt` scoring has been upgraded to a scikit-learn
RandomForest served as a FastAPI HTTP service. The app queries it for the risk
score and falls back to the heuristic engine when the service is unreachable.

## ML pipeline files (repo root)

- `generate_training_data.py` — 4,000 rows + 10 anchor rows -> `data/training_data.csv`.
  Labels come from a Python port of `RiskEngine.kt`; `N_ROWS` env var overrides the size.
- `train_model.py` — trains RandomForest (R2 ~0.92), prints anti-hallucination probe
  checks, saves `vitalis_model.pkl`. Retrain with `.venv/bin/python train_model.py`.
- `main.py` — FastAPI service: `GET /health`, `POST /predict` (accepts camelCase
  RiskInputs as `{"row": {...}}` or `{"features": [...]}`), returns `{"risk_score": x}`.
  Empty payloads -> 4xx so the app falls back to the heuristic. `/docs` for playground.
- `app.py` — old Flask version, no longer used (FastAPI `main.py` replaces it).
- `requirements.txt`, `Dockerfile`, `render.yaml`, `.dockerignore` — deploy config.
- `.venv/` — virtualenv with fastapi, uvicorn, pandas, joblib, scikit-learn.

## Kotlin wiring (already done + compiles)

- `app/src/main/java/com/synq/app/data/risk/MlRiskClient.kt` — Retrofit client.
  BASE_URL is `http://10.0.2.2:5000/` (Android emulator -> dev machine).
- `RiskEngine.calculate(inputs, mlRiskScore: Double?)` — ML score overrides heuristic.
- `SynqViewModel.kt` — fetches/caches the ML score on every weather/health/baseline change;
  `UiState.mlRiskScore` drives it; `HomeScreen` passes it through.
- `AndroidManifest.xml` — `usesCleartextTraffic` enabled for local HTTP.

## NEXT SESSION — deploy ML service + wire app to live URL

Done: SSH key set up, repo pushed to `github.com:Sunlesswastaken/SYNQ.git`.

Remaining:

1. Deploy FastAPI service (Render recommended): go to render.com > New > Blueprint > select SYNQ repo.
   `render.yaml` + `Dockerfile` already in repo, should auto-detect.
   Railway is an alternative (detects Dockerfile automatically).
2. Verify deployment: `curl https://<url>/health` and POST a `/predict` sample.
3. In `app/src/main/java/com/synq/app/data/risk/MlRiskClient.kt` change
   `BASE_URL` from `http://10.0.2.2:5000/` to `https://<live-url>/`.
4. Rebuild APK: `./gradlew :app:installDebug`.
5. Optional: on-device ONNX route via `skl2onnx` — ask if wanted.

Running locally for the emulator: start uvicorn, then `./gradlew :app:installDebug`.