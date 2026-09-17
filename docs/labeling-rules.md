# Classification Engine: Labeling Rules & Bootstrapping Disclosure

This document describes the synthetic data generation logic and labeling rule set used to train version 1 (`v1`) of the Shadow Sentinel classification model.

> [!IMPORTANT]
> **Bootstrapping Transparency Disclosure**:
> Version 1 of this model (`classifier-v1`) was bootstrapped on rule-labeled synthetic data generated to reflect enterprise telemetry patterns across known AI interfaces and non-AI web applications.
> Production deployments will periodically retrain and fine-tune this model on verified, human-labeled telemetry evidence captured in accordance with enterprise privacy policies.

---

## Target Classes (4 Final Labels)

| Label | Description | Operational Criteria |
|---|---|---|
| **`NON_AI`** | Standard web browsing with no AI capabilities detected. | Zero AI UI signals, non-AI domain, no conversational components, no AI interaction. |
| **`AI_CAPABLE_PAGE`** | User is visiting an AI-capable page, but has not interacted with it. | AI UI elements detected (chat interface, prompt input, or known AI domain), but interaction counters are zero. |
| **`AI_INTERACTION`** | User actively engaged with an AI interface. | User has typed prompts, clicked buttons, or pasted text, but output generation has not completed or output was not consumed. |
| **`AI_GENERATION`** | Confirmed generative AI interaction with output consumption. | Generation action triggered (`generateClickCount > 0` or `promptSubmitCount > 0`) **AND** output consumed (`copyFromResponseCount > 0` or `streamingOutputPresent = True`). |

---

## Synthetic Data Generation Rules

The 2,000 synthetic training rows are generated according to the following probabilistic and deterministic rule sets:

### 1. `NON_AI` Class (~500 samples)
- `isKnownAiDomain`: `False`
- `chatInterfacePresent`: `False`
- `promptInputPresent`: `False`
- `generateControlPresent`: `False`
- `regenerateControlPresent`: `False`
- `streamingOutputPresent`: `False`
- `aiTermCount`: `0` (or rarely `1` for false-positive noise)
- `promptSubmitCount`: `0`
- `generateClickCount`: `0`
- `pasteEventCount`: `0` (or occasional paste in regular forms)
- `copyFromResponseCount`: `0`
- `typedCharCountBucket`: `0` (or `1` for non-AI input)

### 2. `AI_CAPABLE_PAGE` Class (~500 samples)
- `isKnownAiDomain`: `True` (85%) or `False` (15% for internal/enterprise AI tools)
- At least one of:
  - `chatInterfacePresent`: `True`
  - `promptInputPresent`: `True`
  - `aiTermCount`: &ge; 2
- **Zero user engagement**:
  - `promptSubmitCount`: `0`
  - `generateClickCount`: `0`
  - `copyFromResponseCount`: `0`
  - `typedCharCountBucket`: `0`
  - `durationSeconds`: typically shorter dwell time (5s - 60s)

### 3. `AI_INTERACTION` Class (~500 samples)
- `chatInterfacePresent`: `True` (90%)
- `promptInputPresent`: `True` (95%)
- `generateControlPresent`: `True` (90%)
- **Active interaction present**:
  - `typedCharCountBucket`: `1` to `4`
  - `promptSubmitCount`: &ge; 1 (or `generateClickCount` &ge; 1)
  - `pasteEventCount`: 0 to 5
- **No output consumption**:
  - `copyFromResponseCount`: `0`
  - `streamingOutputPresent`: `False`

### 4. `AI_GENERATION` Class (~500 samples)
- `chatInterfacePresent`: `True`
- `promptInputPresent`: `True`
- `generateControlPresent`: `True`
- **Active Generation & Output Consumption**:
  - `promptSubmitCount`: &ge; 1 OR `generateClickCount`: &ge; 1
  - AND at least one of:
    - `copyFromResponseCount`: &ge; 1
    - `streamingOutputPresent`: `True`
  - `regenerateControlPresent`: frequently `True` (70%)
  - `typedCharCountBucket`: `1` to `4`
  - `durationSeconds`: longer dwell time (60s - 1200s)
