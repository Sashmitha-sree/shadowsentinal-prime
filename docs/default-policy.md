# Default Corporate AI Policy

## Overview
The default policy ("Default Corporate AI Policy", version 1) is automatically seeded on system initialization if no active policy exists. It provides baseline risk guardrails across enterprise Generative AI usage.

---

## Seeded Policy Rules

| Rule Key | Severity | Score Weight | Description | Conditions |
|---|---|---|---|---|
| **`RULE_FILE_UPLOAD`** | `HIGH` | `30` | File uploaded to an AI service | `requiresFileUpload = true` |
| **`RULE_PASTE_PROMPT`** | `MEDIUM` | `15` | Pasting content into an AI prompt | `requiresPasteEvent = true` |
| **`RULE_HIGH_GEN_VOLUME`** | `HIGH` | `25` | High volume of generation requests | `minGenerateClicks = 5` |
| **`RULE_UNAPPROVED_AI_DOMAIN`** | `MEDIUM` | `20` | Interaction on an unapproved external AI service | `domainPattern = *chatgpt.com*` |
| **`RULE_CRITICAL_DATA_EXFIL`** | `CRITICAL` | `35` | File upload combined with AI Generation | `appliesToLabel = AI_GENERATION`, `requiresFileUpload = true` |

---

## Rule Details & Risk Rationale

### 1. `RULE_FILE_UPLOAD`
- **Key**: `RULE_FILE_UPLOAD`
- **Rationale**: Uploading documents, spreadsheets, or code files to third-party AI models risks exposing proprietary corporate data or credentials.
- **Conditions**: `requiresFileUpload: true`

### 2. `RULE_PASTE_PROMPT`
- **Key**: `RULE_PASTE_PROMPT`
- **Rationale**: Copy-pasting data directly into prompt fields frequently involves internal communications, source snippets, or customer data.
- **Conditions**: `requiresPasteEvent: true`

### 3. `RULE_HIGH_GEN_VOLUME`
- **Key**: `RULE_HIGH_GEN_VOLUME`
- **Rationale**: Sustained or high-frequency prompt generations suggest extensive, programmatic, or non-trivial operational reliance on third-party AI services.
- **Conditions**: `minGenerateClicks: 5`

### 4. `RULE_UNAPPROVED_AI_DOMAIN`
- **Key**: `RULE_UNAPPROVED_AI_DOMAIN`
- **Rationale**: Usage of consumer-grade external AI endpoints (`*chatgpt.com*`) without enterprise data protection agreements.
- **Conditions**: `domainPattern: "*chatgpt.com*"`

### 5. `RULE_CRITICAL_DATA_EXFIL`
- **Key**: `RULE_CRITICAL_DATA_EXFIL`
- **Rationale**: Active generation and file ingestion occurring in the same session represents potential data synthesis and exfiltration.
- **Conditions**: `appliesToLabel: AI_GENERATION`, `requiresFileUpload: true`
