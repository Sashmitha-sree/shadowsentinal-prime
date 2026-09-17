# Risk Model & Scoring Specification

## 1. Overview
The Shadow Sentinel Risk Engine is a 100% deterministic, rule-based scoring engine designed to assess the enterprise risk associated with user browser activity involving Generative AI services.

It combines the machine-learning classification label with active corporate policy rules, applies confidence-adjusted weighting, and outputs an explainable risk score, risk level, and human-readable reasoning summary.

---

## 2. Base Score Mapping
The base risk score is determined directly from the activity's classified `ClassLabel`:

| Classification Label | Base Risk Score | Description |
|---|---|---|
| **`NON_AI`** | `0` | Standard web traffic; no AI risk. |
| **`AI_CAPABLE_PAGE`** | `10` | Passive presence on AI platforms or tools. |
| **`AI_INTERACTION`** | `25` | User prompted, submitted text, or clicked interactive controls. |
| **`AI_GENERATION`** | `40` | AI output was generated and consumed (copied/streamed). |

---

## 3. Policy Rule Evaluation & Weights
Each active policy contains a set of rules. A rule matches an activity if **all specified conditions** are satisfied:

- **`appliesToLabel`**: Null matches any label; otherwise matches if `classificationResult.classLabel == appliesToLabel`.
- **`domainPattern`**: Null matches any domain; otherwise evaluates as a case-insensitive glob pattern against `activity.domain` (e.g. `*openai.com*`, `*.ai`, `chatgpt.com`).
- **`minGenerateClicks`**: If set, requires `evidence.generateClickCount >= minGenerateClicks`.
- **`requiresFileUpload`**: If set to `true`, requires `evidence.fileUploadPresent == true`.
- **`requiresPasteEvent`**: If set to `true`, requires `evidence.pasteEventCount > 0`.

Each matched rule adds its `scoreWeight` (an integer between 0 and 40) to the risk score:
$$\text{Matched Weights} = \sum_{r \in \text{MatchedRules}} r.\text{scoreWeight}$$

---

## 4. Score Arithmetic & Confidence Dampening

1. **Raw Score**:
   $$\text{RawScore} = \text{BaseScore} + \text{Matched Weights}$$

2. **Low-Confidence Dampening**:
   If the classification confidence is below 0.60 ($< 0.60$), the classification is considered uncertain, and the score is dampened:
   $$\text{AdjustedScore} = \begin{cases} \text{round}(\text{RawScore} \times 0.7) & \text{if } \text{confidence} < 0.60 \\ \text{RawScore} & \text{if } \text{confidence} \ge 0.60 \end{cases}$$

3. **Clamping**:
   $$\text{FinalScore} = \min(100, \max(0, \text{AdjustedScore}))$$

---

## 5. Risk Level Boundaries

The final clamped score is mapped to a discrete `RiskLevel`:

| Score Range | Risk Level | Description |
|---|---|---|
| **0 – 24** | **`LOW`** | Benign or low-risk usage; standard monitoring. |
| **25 – 49** | **`MEDIUM`** | Moderate risk; basic AI interactions or unverified queries. |
| **50 – 74** | **`HIGH`** | Elevated risk; file uploads, pasting data, or high usage volume. |
| **75 – 100** | **`CRITICAL`** | Severe risk; sensitive data exfiltration or policy violations. |

### Exact Boundary Test Matrix:
- Score `24` &rarr; `LOW`
- Score `25` &rarr; `MEDIUM`
- Score `49` &rarr; `MEDIUM`
- Score `50` &rarr; `HIGH`
- Score `74` &rarr; `HIGH`
- Score `75` &rarr; `CRITICAL`

---

## 6. Explainability Requirement

Every risk assessment stores a human-readable `reasoning` string (max 1000 characters) designed for non-technical security reviewers. It details:
1. **Detected Class & Confidence**: e.g., `AI_GENERATION (confidence: 95%)`.
2. **Base Risk Contribution**: e.g., `Base Score: 40`.
3. **Matched Policy Rules**: Description and added weight for each triggered rule.
4. **Score Arithmetic**: Explicit step-by-step formula including any low-confidence adjustments.
5. **Final Assessment**: Clamped score and assigned risk level.
