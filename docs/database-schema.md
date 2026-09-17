# Shadow Sentinel Database Schema Specification

This document details the complete relational database schema for the Shadow Sentinel system, mapped directly from the JPA entities in `backend/src/main/java/com/shadowsentinel/`.

---

## 1. Text Entity-Relationship (ER) Diagram

```text
  +-----------------------+
  |         users         |
  +-----------------------+
  | PK  id                |
  |     email (UQ)        |
  |     password          |
  |     role              |
  |     company_id        |
  |     created_at        |
  +-----------------------+
       |               |
       | 1:N           | 1:N
       v               v
+------------------+  +-----------------------+
| browser_sessions |  |        alerts         |
+------------------+  +-----------------------+
| PK  id           |  | PK  id                |
| FK  user_id      |  | FK  user_id           |
|     started_at   |  | FK  activity_id       |
|     ended_at     |  | FK  risk_assessment_id|
|     status       |  |     severity          |
|     created_at   |  |     title             |
+------------------+  |     message           |
       |              |     status            |
       | 1:N          |     occurrence_count  |
       v              |     created_at        |
+----------------------+     acknowledged_at   |
|  browser_activities  |  +-----------------------+
+----------------------+         ^           ^
| PK  id               |         |           |
| FK  session_id       |         |           |
|     domain           |---------+           |
|     url              |                     |
|     page_title       |                     |
|     started_at       |                     |
|     ended_at         |                     |
|     duration_seconds |                     |
|     created_at       |                     |
+----------------------+                     |
   |           |          |                  |
   | 1:1       | 1:1      | 1:1              |
   v           v          v                  |
+-------------------------+  +-------------------------+  +-------------------------+
| classification_evidence |  | classification_results  |  |    risk_assessments     |
+-------------------------+  +-------------------------+  +-------------------------+
| PK  id                  |  | PK  id                  |  | PK  id                  |
| FK  activity_id (UQ)    |  | FK  activity_id (UQ)    |  | FK  activity_id (UQ)    |---+
|     schema_version      |  |     class_label         |  |     risk_score          |
|     captured_at         |  |     confidence          |  |     risk_level          |
|     created_at          |  |     model_version       |  |     matched_rule_ids    |
|     domain_length       |  |     created_at          |  |     reasoning           |
|     visit_count         |  +-------------------------+  |     policy_version      |
|     duration_seconds    |                               |     model_version       |
|     is_known_ai_domain  |                               |     created_at          |
|     hour_of_day         |                               +-------------------------+
|     path_depth          |
|     chat_interface_pres |
|     prompt_input_pres   |
|     generate_ctrl_pres  |
|     regen_ctrl_pres     |
|     ai_term_count       |
|     stream_output_pres  |
|     file_upload_pres    |
|     prompt_submit_count |
|     generate_click_count|
|     paste_event_count   |
|     copy_response_count |
|     typed_char_bucket   |
+-------------------------+

+--------------------+          +---------------------+          +----------------------+
|  company_policies  |          |    policy_rules     |          |      audit_logs      |
+--------------------+          +---------------------+          +----------------------+
| PK  id             |          | PK  id              |          | PK  id               |
|     company_id     |          | FK  policy_id       |          |     user_id          |
|     name           | 1:N      |     rule_key        |          |     event_type       |
|     active         |--------->|     applies_to_label|          |     target_id        |
|     version        |          |     domain_pattern  |          |     details          |
|     created_at     |          |     min_generate_cl |          |     created_at       |
+--------------------+          |     requires_file_up|          +----------------------+
                                |     requires_paste  |
                                |     severity        |
                                |     score_weight    |
                                |     description     |
                                +---------------------+
```

---

## 2. Table Specifications

### 2.1 Table: `users`
Represents registered system users (analysts and administrators).

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `email` | VARCHAR(255) | NO | | Unique constraint (`UK_users_email`) |
| `password` | VARCHAR(255) | NO | | BCrypt hashed credential |
| `role` | VARCHAR(32) | NO | | Enum: `ANALYST`, `ADMIN` |
| `company_id` | BIGINT | YES | NULL | Optional enterprise organization reference |
| `created_at` | TIMESTAMP | NO | `now()` | Immutable creation timestamp |

- **Foreign Keys**: None
- **Relationships**:
  - `1:N` to `browser_sessions` via `browser_sessions.user_id`
  - `1:N` to `alerts` via `alerts.user_id`

---

### 2.2 Table: `browser_sessions`
Captures browser execution sessions created by the browser extension.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `user_id` | BIGINT | NO | | Foreign Key -> `users(id)` |
| `started_at` | TIMESTAMP | NO | | Session start timestamp |
| `ended_at` | TIMESTAMP | YES | NULL | Session termination timestamp |
| `status` | VARCHAR(32) | NO | `ACTIVE` | Enum: `ACTIVE`, `CLOSED` |
| `created_at` | TIMESTAMP | NO | `now()` | Immutable creation timestamp |

- **Foreign Keys**: `FOREIGN KEY (user_id) REFERENCES users(id)`
- **Relationships**:
  - `N:1` to `users`
  - `1:N` to `browser_activities` via `browser_activities.session_id`

---

### 2.3 Table: `browser_activities`
Represents individual domain navigation events within a browser session.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `session_id` | BIGINT | NO | | Foreign Key -> `browser_sessions(id)` |
| `domain` | VARCHAR(255) | NO | | Normalized domain hostname (e.g. `chatgpt.com`) |
| `url` | VARCHAR(500) | YES | NULL | Sanitized URL path |
| `page_title` | VARCHAR(255) | YES | NULL | Document title captured from page |
| `started_at` | TIMESTAMP | NO | | Activity activation timestamp |
| `ended_at` | TIMESTAMP | YES | NULL | Activity transition timestamp |
| `duration_seconds` | INTEGER | YES | NULL | Computed duration in seconds |
| `created_at` | TIMESTAMP | NO | `now()` | Immutable creation timestamp |

- **Indexes**:
  - `idx_browser_activities_session_id` ON `(session_id)`
  - `idx_browser_activities_domain` ON `(domain)`
- **Foreign Keys**: `FOREIGN KEY (session_id) REFERENCES browser_sessions(id)`
- **Relationships**:
  - `N:1` to `browser_sessions`
  - `1:1` to `classification_evidence`
  - `1:1` to `classification_results`
  - `1:1` to `risk_assessments`
  - `1:N` to `alerts` via `alerts.activity_id`

---

### 2.4 Table: `classification_evidence`
Stores the frozen 18-signal evidence vector collected by the browser extension.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `activity_id` | BIGINT | NO | | Foreign Key -> `browser_activities(id)`, Unique |
| `schema_version`| VARCHAR(32) | NO | `'v1'` | Contract version |
| `capturedAt` | TIMESTAMP | NO | | Client-side capture timestamp |
| `created_at` | TIMESTAMP | NO | `now()` | Immutable ingestion timestamp |
| **Metadata Signals (6)** | | | | |
| `domain_length` | INTEGER | NO | 0 | Length of target domain name |
| `visit_count` | INTEGER | NO | 0 | User historical visit count for this domain |
| `duration_seconds` | INTEGER | NO | 0 | Dwell time on page |
| `is_known_ai_domain`| BOOLEAN | NO | FALSE | True if domain matches curated AI domains |
| `hour_of_day` | INTEGER | NO | 0 | Hour of access (0-23) |
| `path_depth` | INTEGER | NO | 0 | URL path segments count |
| **UI Signals (7)** | | | | |
| `chat_interface_present` | BOOLEAN | NO | FALSE | Chat dialogue UI elements detected |
| `prompt_input_present` | BOOLEAN | NO | FALSE | Textarea or prompt input detected |
| `generate_control_present`| BOOLEAN | NO | FALSE | Generate / Submit button detected |
| `regenerate_control_present`| BOOLEAN | NO | FALSE | Regenerate response button detected |
| `ai_term_count` | INTEGER | NO | 0 | Occurrences of AI-related terminology |
| `streaming_output_present`| BOOLEAN | NO | FALSE | Progressive SSE / streaming text detected |
| `file_upload_present` | BOOLEAN | NO | FALSE | File attachment control detected |
| **Interaction Signals (5)** | | | | |
| `prompt_submit_count` | INTEGER | NO | 0 | Total prompts submitted |
| `generate_click_count` | INTEGER | NO | 0 | Total generate button clicks |
| `paste_event_count` | INTEGER | NO | 0 | Total paste events into input controls |
| `copy_from_response_count`| INTEGER | NO | 0 | Copy events from generated responses |
| `typed_char_count_bucket` | INTEGER | NO | 0 | Bucketed characters typed (0: 0, 1: 1-50, 2: 51-200, 3: 201-1000, 4: 1001+) |

- **Foreign Keys**: `FOREIGN KEY (activity_id) REFERENCES browser_activities(id)`
- **Unique Constraints**: `UNIQUE (activity_id)`

---

### 2.5 Table: `classification_results`
Stores the ML model inference output for an activity.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `activity_id` | BIGINT | NO | | Foreign Key -> `browser_activities(id)`, Unique |
| `class_label` | VARCHAR(32) | NO | | Enum: `NON_AI`, `AI_CAPABLE_PAGE`, `AI_INTERACTION`, `AI_GENERATION` |
| `confidence` | DOUBLE PRECISION | NO | | Model classification confidence (0.00 - 1.00) |
| `model_version` | VARCHAR(32) | NO | | Model artifact version (e.g. `'v1'`) |
| `created_at` | TIMESTAMP | NO | `now()` | Immutable classification timestamp |

- **Foreign Keys**: `FOREIGN KEY (activity_id) REFERENCES browser_activities(id)`
- **Unique Constraints**: `UNIQUE (activity_id)`

---

### 2.6 Table: `company_policies`
Defines enterprise governance rulesets for evaluating risk.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `company_id` | BIGINT | YES | NULL | Target organization identifier |
| `name` | VARCHAR(255) | NO | | Policy name (e.g. `'Default AI Usage Policy'`) |
| `active` | BOOLEAN | NO | TRUE | Active status indicator |
| `version` | INTEGER | NO | 1 | Incremented on rule additions |
| `created_at` | TIMESTAMP | NO | `now()` | Creation timestamp |

- **Foreign Keys**: None
- **Relationships**:
  - `1:N` to `policy_rules` via `policy_rules.policy_id`

---

### 2.7 Table: `policy_rules`
Defines individual deterministic rules within a company policy.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `policy_id` | BIGINT | NO | | Foreign Key -> `company_policies(id)` |
| `rule_key` | VARCHAR(255) | NO | | Unique identifier code (e.g. `'RULE_FILE_UPLOAD'`) |
| `applies_to_label` | VARCHAR(32) | YES | NULL | Enum: `NON_AI`, `AI_CAPABLE_PAGE`, `AI_INTERACTION`, `AI_GENERATION` (NULL = all) |
| `domain_pattern` | VARCHAR(255) | YES | NULL | Simple glob pattern (e.g. `'*sensitive.com*'`) |
| `min_generate_clicks` | INTEGER | YES | NULL | Minimum generate clicks condition |
| `requires_file_upload`| BOOLEAN | YES | NULL | Condition matching `fileUploadPresent` |
| `requires_paste_event` | BOOLEAN | YES | NULL | Condition matching `pasteEventCount > 0` |
| `severity` | VARCHAR(32) | NO | | Enum: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `score_weight` | INTEGER | NO | 0 | Risk score increment (0 - 40) |
| `description` | VARCHAR(255) | NO | | Human-readable explanation of rule purpose |

- **Foreign Keys**: `FOREIGN KEY (policy_id) REFERENCES company_policies(id)` ON DELETE CASCADE

---

### 2.8 Table: `risk_assessments`
Records the deterministic risk engine evaluation for each activity.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `activity_id` | BIGINT | NO | | Foreign Key -> `browser_activities(id)`, Unique |
| `risk_score` | INTEGER | NO | | Evaluated risk score (0 to 100, clamped) |
| `risk_level` | VARCHAR(32) | NO | | Enum: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `matched_rule_ids` | VARCHAR(255) | NO | | Comma-separated list of triggered rule keys |
| `reasoning` | VARCHAR(1000) | NO | | Detailed auditable explanation of score |
| `policy_version` | INTEGER | NO | | Version of company policy evaluated |
| `model_version` | VARCHAR(32) | NO | | Model version from classification result |
| `created_at` | TIMESTAMP | NO | `now()` | Assessment timestamp |

- **Foreign Keys**: `FOREIGN KEY (activity_id) REFERENCES browser_activities(id)`
- **Unique Constraints**: `UNIQUE (activity_id)`
- **Relationships**:
  - `1:N` to `alerts` via `alerts.risk_assessment_id`

---

### 2.9 Table: `alerts`
Security notifications generated when an activity evaluates to `HIGH` or `CRITICAL` risk.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `user_id` | BIGINT | NO | | Foreign Key -> `users(id)` |
| `activity_id` | BIGINT | NO | | Foreign Key -> `browser_activities(id)` |
| `risk_assessment_id` | BIGINT | NO | | Foreign Key -> `risk_assessments(id)` |
| `severity` | VARCHAR(32) | NO | | Enum: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| `title` | VARCHAR(255) | NO | | Alert summary (e.g. `'CRITICAL Risk Detected on chatgpt.com'`) |
| `message` | VARCHAR(2000) | NO | | Full explanation and context |
| `status` | VARCHAR(32) | NO | `'NEW'` | Enum: `NEW`, `ACKNOWLEDGED`, `RESOLVED` |
| `occurrence_count` | INTEGER | NO | 1 | Incremented when deduplicated within 30 minutes |
| `created_at` | TIMESTAMP | NO | `now()` | Initial alert creation timestamp |
| `acknowledged_at` | TIMESTAMP | YES | NULL | Timestamp when analyst acknowledged alert |

- **Foreign Keys**:
  - `FOREIGN KEY (user_id) REFERENCES users(id)`
  - `FOREIGN KEY (activity_id) REFERENCES browser_activities(id)`
  - `FOREIGN KEY (risk_assessment_id) REFERENCES risk_assessments(id)`

---

### 2.10 Table: `audit_logs`
Immutable record of security and administrative operations across the platform.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | BIGINT | NO | Auto-Increment | Primary Key |
| `user_id` | BIGINT | YES | NULL | User who initiated action (NULL for system events) |
| `event_type` | VARCHAR(64) | NO | | Enum: `LOGIN`, `LOGIN_FAILED`, `SESSION_STARTED`, `EVIDENCE_INGESTED`, `CLASSIFICATION_CREATED`, `RISK_ASSESSED`, `ALERT_CREATED`, `ALERT_ACKNOWLEDGED`, `POLICY_CHANGED` |
| `target_id` | VARCHAR(255) | YES | NULL | ID of affected entity (e.g. alertId, activityId) |
| `details` | VARCHAR(2000) | YES | NULL | Human-readable audit description |
| `created_at` | TIMESTAMP | NO | `now()` | Immutable timestamp of event |

- **Foreign Keys**: None (preserves audit integrity even if target entities are purged)
