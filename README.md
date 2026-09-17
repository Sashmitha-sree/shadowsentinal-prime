# Shadow Sentinel

Shadow Sentinel is a security and threat detection monorepo integrating a Chrome MV3 extension, a Java 21 / Spring Boot 3 backend service, and a Python / FastAPI ML microservice backed by PostgreSQL 16.

---

## Monorepo Layout

```
shadow-sentinel/
├── extension/          # Chrome MV3 extension (vanilla JS, no build step)
├── backend/            # Java 21 + Spring Boot 3.x (Maven)
├── ml-service/         # Python 3.11 + FastAPI + scikit-learn
├── docs/               # Architecture and design specifications
├── docker-compose.yml  # PostgreSQL 16 container setup
├── .env.example        # Environment variable template
├── .gitignore          # Repository gitignore rules
└── README.md           # Project guide and local setup instructions
```

---

## Prerequisites

- **Docker & Docker Compose** (for PostgreSQL)
- **Java 21+** (JDK 21 or later)
- **Apache Maven 3.9+**
- **Python 3.11+**
- **Google Chrome** (or any Chromium-based browser)

---

## Local Setup Guide

### 1. Database (PostgreSQL 16)

1. Copy the sample environment file:
   ```bash
   cp .env.example .env
   ```
   *(On Windows PowerShell: `Copy-Item .env.example .env`)*

2. Start the PostgreSQL container:
   ```bash
   docker compose up -d
   ```

3. Confirm PostgreSQL is running:
   ```bash
   docker compose ps
   ```
   PostgreSQL will be exposed on port `5432` with database `shadow_sentinel` and user `sentinel`.

---

### 2. Backend Service (`backend/`)

1. Compile the project and download Maven dependencies:
   ```bash
   mvn -f backend/pom.xml compile
   ```

2. Run the Spring Boot application:
   ```bash
   mvn -f backend/pom.xml spring-boot:run
   ```
   The backend service starts on `http://localhost:8080`.

---

### 3. Machine Learning Service (`ml-service/`)

1. Navigate to the `ml-service` folder:
   ```bash
   cd ml-service
   ```

2. Create and activate a Python virtual environment:
   - **Linux / macOS**:
     ```bash
     python3 -m venv .venv
     source .venv/bin/activate
     ```
   - **Windows (PowerShell)**:
     ```powershell
     python -m venv .venv
     .venv\Scripts\Activate.ps1
     ```

3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

4. Start the FastAPI development server:
   ```bash
   uvicorn app.main:app --reload --port 8000
   ```
   The ML service will be accessible at `http://localhost:8000`. Interactive API docs are available at `http://localhost:8000/docs`.

---

### 4. Chrome Extension (`extension/`)

The extension uses vanilla JavaScript with Chrome Manifest V3 and requires **no build step**.

1. Open Google Chrome and navigate to `chrome://extensions`.
2. Toggle **Developer mode** in the top right corner to **ON**.
3. Click **Load unpacked**.
4. Select the `extension/` folder inside this repository (`c:\MyCreation\shadowsentinal\extension`).
5. The **Shadow Sentinel** icon will appear in your extensions toolbar. Click the icon to view the popup status.

---

## Verifying the Build

Run the Maven compile step from the root of the repository:

```bash
mvn -q -f backend/pom.xml compile
```
