# Shadow Sentinel: Enterprise Generative AI Telemetry & Governance

Shadow Sentinel is an enterprise security monitoring and threat detection system for employee Generative AI interactions. It combines a zero-overhead Chrome MV3 browser extension, a resilient Spring Boot 3 / Java 21 backend, a high-precision Scikit-Learn / FastAPI microservice, and an embedded administrative dashboard backed by PostgreSQL 16.

---

## Architecture Overview

```
 [ Chrome Browser Extension (MV3) ]
           │
           │ (18 telemetry signals via REST)
           ▼
 [ Spring Boot Backend (:8080) ] ────▶ [ PostgreSQL 16 (:5432) ]
      │                   ▲
      │ (Async Events)    │ (Inference / Predict)
      ▼                   │
 [ Rule Risk Engine ]     ▼
      │             [ ML Microservice (:8000) ]
      ▼
 [ Alerts & Audit Log ] ──▶ [ Static Web Dashboard (:8080/index.html) ]
```

---

## Monorepo Layout

```
shadow-sentinel/
├── backend/            # Java 21 + Spring Boot 3.3 (REST APIs, Security, Risk Engine, Dashboard)
│   ├── src/main/java/  # Auth, Browser, Classification, Risk, Alert, Audit modules
│   ├── src/main/resources/static/ # Static Admin Dashboard (HTML, Vanilla CSS, JS)
│   └── src/test/java/  # Unit, MockMvc, and End-to-End Testcontainers tests
├── ml-service/         # Python 3.11+ / FastAPI / scikit-learn classifier
│   ├── app/            # FastAPI app, Pydantic schemas, Model inference
│   ├── data/           # Ground truth training dataset (18 signals contract)
│   ├── models/         # Serialized RandomForest model (.joblib) & metadata
│   └── tests/          # Pytest integration test suite
├── extension/          # Chrome MV3 extension (Vanilla JS, non-blocking telemetry)
│   ├── collectors/     # Metadata, UI signals, and User interaction collectors
│   ├── popup.*         # Extension status UI & token setup
│   └── background.js   # Service worker handling session lifecycle & ingestion
├── docs/               # Technical specs (API, DB Schema, Risk Model, Status)
├── docker-compose.yml  # PostgreSQL 16 container definition
├── .env.example        # Environment variable configuration template
└── README.md           # Setup and presentation guide
```

---

## Prerequisites

Before starting, ensure your system has the following tools installed:

1. **Java 21+** (JDK 21 or higher)
   - Verify: `java -version`
2. **Apache Maven 3.9+**
   - Verify: `mvn -version`
3. **Python 3.11+**
   - Verify: `python --version`
4. **Docker & Docker Compose** (Optional for local PostgreSQL container; automated tests fall back gracefully if Docker is absent)
   - Verify: `docker compose version`
5. **Google Chrome** (or any Chromium-based browser such as Brave or Edge)

---

## Quickstart Setup Guide

### 1. Start PostgreSQL 16

Start the database using Docker Compose:
```bash
docker compose up -d
```
*PostgreSQL will be accessible at `localhost:5432` with username `sentinel`, password `sentinel_secret`, and database `shadow_sentinel`.*

*(Note: If running without Docker, configure your local PostgreSQL instance matching the credentials in `backend/src/main/resources/application.yml` or run with H2 test profile).*

---

### 2. Start the Machine Learning Microservice

The ML service provides sub-10ms classification across 4 classes (`NON_AI`, `AI_CAPABLE_PAGE`, `AI_INTERACTION`, `AI_GENERATION`).

1. Navigate to `ml-service/`:
   ```bash
   cd ml-service
   ```
2. Create and activate a virtual environment:
   - **Linux / macOS**:
     ```bash
     python3 -m venv .venv
     source .venv/bin/activate
     ```
   - **Windows (PowerShell)**:
     ```powershell
     python -m venv .venv
     .\.venv\Scripts\Activate.ps1
     ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Run the FastAPI microservice:
   ```bash
   uvicorn app.main:app --port 8000 --reload
   ```
   - Health check: `http://localhost:8000/health`
   - Interactive Swagger Docs: `http://localhost:8000/docs`

---

### 3. Start the Spring Boot Backend

The backend exposes the REST API, coordinates event-driven risk evaluation, and serves the static security dashboard.

1. From the repository root, compile and run the backend:
   ```bash
   mvn -f backend/pom.xml spring-boot:run
   ```
2. The service starts on `http://localhost:8080`.
   - Access the Security Dashboard: `http://localhost:8080/index.html`

---

### 4. Load the Chrome Extension Unpacked

The Chrome MV3 extension monitors browser activity without external build tools.

1. Open Google Chrome and navigate to `chrome://extensions`.
2. Enable **Developer mode** using the toggle switch in the top-right corner.
3. Click **Load unpacked** in the top-left toolbar.
4. Select the `extension/` directory from this repository:
   ```text
   <path-to-repository>/extension
   ```
5. The **Shadow Sentinel** shield icon will appear in your Chrome toolbar. Click the icon to view status, connection state, and active session ID.

---

## 6-Step Demo Script for Live Presentations

Follow this tested, sequential demonstration script to showcase the entire Shadow Sentinel lifecycle during a live presentation or evaluation:

```
┌─────────┐      ┌─────────┐      ┌─────────┐      ┌─────────┐      ┌─────────┐      ┌─────────┐
│ Step 1  │ ───▶ │ Step 2  │ ───▶ │ Step 3  │ ───▶ │ Step 4  │ ───▶ │ Step 5  │ ───▶ │ Step 6  │
│ Launch  │      │ Login   │      │ Connect │      │ Normal  │      │ Data    │      │ Alert   │
│ Stack   │      │ Admin   │      │ Browser │      │ Prompt  │      │ Exfil   │      │ Triage  │
└─────────┘      └─────────┘      └─────────┘      └─────────┘      └─────────┘      └─────────┘
```

### Step 1: Launch Stack & Open the Dashboard
- Start PostgreSQL (`docker compose up -d`), ML Service (`uvicorn app.main:app --port 8000`), and Backend (`mvn spring-boot:run`).
- Open your browser to `http://localhost:8080/index.html`.
- **Talking Point**: *"Shadow Sentinel operates with a decoupled architecture: an edge browser collector, an asynchronous Spring Boot backend, and a high-speed Python ML inference service."*

### Step 2: Authenticate as Security Analyst
- On the dashboard, click **Login** (or Register if first run).
- Enter demo credentials:
  - **Email**: `admin@sentinel.local`
  - **Password**: `Secur3P@ssw0rd!`
- Observe the Overview tab load dynamically: zero telemetry delay, real-time KPI cards (Total Activities, Label Distribution, Open Alerts).
- Copy the JWT Bearer token from the console or `sessionStorage.getItem("token")`.

### Step 3: Activate the Browser Extension
- Click the **Shadow Sentinel** extension icon in Chrome.
- Paste the JWT token (or click **Sync from Dashboard**).
- Observe the extension popup turn green: `Status: Connected` and display an active `Session ID`.
- **Talking Point**: *"The extension enforces privacy-by-design. It requests zero invasive permissions—no reading DOM cookies, no raw keystroke capture, and no storage of prompt text. Only 18 mathematical behavioral signals are gathered."*

### Step 4: Normal AI Usage Telemetry
- Navigate to an AI tool (e.g. `chatgpt.com` or `claude.ai`).
- Type a standard query (e.g. *"Explain quantum entanglement"*).
- Switch to the Dashboard **Activities** tab:
  - An activity record appears for `chatgpt.com`.
  - ML Classifier assigns label: `AI_INTERACTION` (confidence > 95%).
  - Risk Engine assigns score: `25` (`LOW` risk).
  - No alert is raised because no sensitive policy is breached.

### Step 5: Trigger a CRITICAL Security Violation (Simulated Exfiltration)
- In the same AI session, simulate an exfiltration attempt:
  - Paste multi-line data into the prompt box (triggers `pasteEventCount > 0`).
  - Click the file attachment / upload button (triggers `fileUploadPresent = true`).
  - Submit the prompt multiple times (triggers `generateClickCount >= 3`).
- **Talking Point**: *"Watch what happens in real time: the browser extension captures the UI and interaction signals, sends them to the backend, the ML model classifies it as `AI_GENERATION`, and the deterministic Risk Engine matches `RULE_FILE_UPLOAD` and `RULE_CRITICAL_DATA_EXFIL`."*

### Step 6: Real-time Alert Triage & Audit Trail
- Click the **Alerts** tab on the Dashboard:
  - A bright red badge appears: **CRITICAL Risk Detected on chatgpt.com**.
  - Risk Score: **100/100**.
- Click the alert row to open the side inspection panel:
  - Show the **18 Signals Breakdown** (file upload present, paste event detected, AI terms detected).
  - Show the **Deterministic Reasoning String** explaining every matched rule weight.
- Click **Acknowledge Alert** -> status transitions to `ACKNOWLEDGED`.
- Click **Resolve Alert** -> status updates to `RESOLVED`.
- Switch to **Audit Log** tab (Admin) and showcase the immutable audit entries: `LOGIN`, `EVIDENCE_INGESTED`, `RISK_ASSESSED`, `ALERT_CREATED`, and `ALERT_ACKNOWLEDGED`.

---

## Automated Test Execution

### Backend Tests (JUnit 5 + MockMvc + Testcontainers)
Run the full backend test suite:
```bash
mvn -f backend/pom.xml test
```
*Executes 63 unit, controller, risk engine, and full end-to-end integration tests (`register -> login -> session -> activity -> evidence -> ML -> risk -> CRITICAL alert`).*

### ML Service Tests (Pytest)
Run the ML service validation suite:
```bash
python -m pytest ml-service/
```
*Validates `/health` check, model inference across all 4 classes, Pydantic schema validation, and boundary conditions.*
