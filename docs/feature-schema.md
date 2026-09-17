# Frozen Feature Contract: Classification Evidence Schema

This document defines the frozen feature schema for Shadow Sentinel's classification engine.
All features are persisted in a single flat table (`classification_evidence`) linked 1:1 with `BrowserActivity`.

> [!IMPORTANT]
> **Privacy Guarantee**: Under no circumstances does this schema or any collector capture raw prompt text, response text, page text, or user keystrokes. `typedCharCountBucket` represents a bucketed magnitude only.

---

## Schema Overview

- **Table Name**: `classification_evidence`
- **Schema Version**: `v1`
- **Total Feature Signals**: 18
  - 6 Metadata Signals
  - 7 UI Signals
  - 5 Interaction Signals
- **Nullability**: All integer and boolean feature columns are non-nullable (`NOT NULL`), with default values `0` and `false` respectively.

---

## Signal Definitions

### 1. Metadata Signals (6)

| # | Field | Type | Valid Range | Default | Semantic Meaning |
|---|---|---|---|---|---|
| 1 | `domainLength` | `int` | `[0, ∞)` | `0` | Character length of the domain (e.g., `chatgpt.com` = 11). |
| 2 | `visitCount` | `int` | `[0, ∞)` | `0` | Frequency count of visits to this domain within the active session / retention window. |
| 3 | `durationSeconds` | `int` | `[0, ∞)` | `0` | Active engagement time on the page in seconds. |
| 4 | `isKnownAiDomain` | `boolean` | `true / false` | `false` | Whether the domain matches a curated directory of known AI/LLM providers (e.g., OpenAI, Anthropic, Mistral). |
| 5 | `hourOfDay` | `int` | `[0, 23]` | `0` | Local or UTC hour of access (0 to 23), used to detect out-of-hours / anomalous activity. |
| 6 | `pathDepth` | `int` | `[0, ∞)` | `0` | Depth of URL path segments (e.g., `/c/chat/123` = 3). |

---

### 2. UI Signals (7)

| # | Field | Type | Valid Range | Default | Semantic Meaning |
|---|---|---|---|---|---|
| 7 | `chatInterfacePresent` | `boolean` | `true / false` | `false` | Presence of conversational DOM elements (message bubbles, conversational stream containers). |
| 8 | `promptInputPresent` | `boolean` | `true / false` | `false` | Presence of multiline textarea, contenteditable, or input fields identified as prompt editors. |
| 9 | `generateControlPresent` | `boolean` | `true / false` | `false` | Presence of submission buttons matching generate / send / submit intent. |
| 10 | `regenerateControlPresent` | `boolean` | `true / false` | `false` | Presence of regeneration, retry, or alternative output controls. |
| 11 | `aiTermCount` | `int` | `[0, ∞)` | `0` | Occurrences of AI-related keywords in UI labels/aria tags (e.g., "Model", "Token", "Prompt", "Temperature"). |
| 12 | `streamingOutputPresent` | `boolean` | `true / false` | `false` | Detection of progressive DOM updates characteristic of streaming token responses. |
| 13 | `fileUploadPresent` | `boolean` | `true / false` | `false` | Presence of attachment buttons, drag-and-drop zones, or file inputs for data exfiltration analysis. |

---

### 3. Interaction Signals (5)

| # | Field | Type | Valid Range | Default | Semantic Meaning |
|---|---|---|---|---|---|
| 14 | `promptSubmitCount` | `int` | `[0, ∞)` | `0` | Number of times the user submitted a prompt or triggered query execution during the activity. |
| 15 | `generateClickCount` | `int` | `[0, ∞)` | `0` | Total clicks recorded on generation / action buttons. |
| 16 | `pasteEventCount` | `int` | `[0, ∞)` | `0` | Frequency of clipboard paste events into input elements (signals potential sensitive data insertion). |
| 17 | `copyFromResponseCount` | `int` | `[0, ∞)` | `0` | Frequency of clipboard copy events originating from AI response containers. |
| 18 | `typedCharCountBucket` | `int` | `[0, 4]` | `0` | Bucketed representation of volume typed, never recording raw text: <br>• `0`: 0 characters<br>• `1`: 1 - 50 characters<br>• `2`: 51 - 250 characters<br>• `3`: 251 - 1000 characters<br>• `4`: > 1000 characters |

---

## Entity Metadata Fields

| Field | Type | Description |
|---|---|---|
| `id` | `Long` (Primary Key) | Auto-incrementing identifier. |
| `activityId` | `Long` (Unique Foreign Key) | 1:1 link to `browser_activities.id`. Unique constraint enforced. |
| `schemaVersion` | `String` | Schema version identifier (defaults to `"v1"`). |
| `capturedAt` | `Instant` | Client-side timestamp when signals were extracted. |
| `createdAt` | `Instant` | Server-side record creation timestamp. |
