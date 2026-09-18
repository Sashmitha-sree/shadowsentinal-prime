# Risk Model & Scoring Specification

## 1. Overview
The Shadow Sentinel Risk Engine is a 100% deterministic, explainable risk scoring engine designed to assess enterprise risk associated with employee browser activities.

In this model, **AI Domain Status (`ai_domains`) is the PRIMARY factor** determining risk, governing whether an activity is capped, elevated, or unconditionally marked as a critical policy violation. Pure interaction-depth scoring is subordinated to administrative domain governance.

---

## 2. Base Score Mapping
The base risk score is determined directly from the activity's classified `ClassLabel`:

| Classification Label | Base Risk Score | Description |
|---|---|---|
| **`NON_AI`** | `0` | Standard web traffic; no AI capabilities detected. |
| **`AI_CAPABLE_PAGE`** | `10` | Passive presence on AI platform or tool. |
| **`AI_INTERACTION`** | `25` | User submitted text, prompted, or interacted with AI controls. |
| **`AI_GENERATION`** | `40` | AI content generated, streamed, or copied. |

---

## 3. Scoring Architecture & Governance Hierarchy

### 3.1 Non-AI Activity Rule
If the classification label is **`NON_AI`**:
- **`riskScore = 0`**
- **`riskLevel = LOW`**
- This applies unconditionally, regardless of policy rules, paste events, uploads, or any domain status. Non-AI traffic can never reach `MEDIUM`, `HIGH`, or `CRITICAL`.
- Reasoning first line: `"Activity classified as non-AI; zero risk assigned."`

---

### 3.2 AI-Related Activity (`AI_CAPABLE_PAGE`, `AI_INTERACTION`, `AI_GENERATION`)
For all AI-related classifications, the engine looks up the activity's domain in the `ai_domains` registry. The scoring formula is governed strictly by the domain's status:

#### A. Status: `BLOCKED`
- **`riskScore = 100`**
- **`riskLevel = CRITICAL`**
- **Immediate Violation**: Overrides interaction depth, matched rules, and confidence dampening. Using a blocked AI service at all constitutes a finding.
- **Confidence Dampening**: **EXEMPT**. Always remains `100` / `CRITICAL` even if ML confidence is `< 0.60`.
- Reasoning first line: `"Domain is on the blocked AI list set by admin."`

#### B. Status: `APPROVED`
- **Capped at `MEDIUM`**: Even with `AI_GENERATION` and maximal rule matches, the risk score is capped so it never exceeds `49`.
- **Reduced Scoring Formula**:
  1. Base score scaled to 25% weight:
     $$\text{EffectiveBaseScore} = \text{round}(\text{BaseScore} \times 0.25)$$
     *(e.g., `AI_CAPABLE_PAGE`: 3, `AI_INTERACTION`: 6, `AI_GENERATION`: 10)*
  2. Matched policy rule weights are summed:
     $$\text{RawScore} = \text{EffectiveBaseScore} + \sum \text{RuleWeights}$$
  3. Low-confidence dampening applies if confidence $< 0.60$:
     $$\text{AdjustedScore} = \begin{cases} \text{round}(\text{RawScore} \times 0.7) & \text{if } \text{confidence} < 0.60 \\ \text{RawScore} & \text{otherwise} \end{cases}$$
  4. Final score clamped to `49` (MEDIUM ceiling):
     $$\text{FinalScore} = \min(49, \max(0, \text{AdjustedScore}))$$
- Reasoning first line: `"Domain is approved for AI use; risk capped accordingly."`

#### C. Status: `UNKNOWN`
*(Includes unreviewed domains auto-inserted upon detection or unlisted domains)*
- **Elevation Formula**:
  1. Standard base score table applies.
  2. Matched policy rule weights are added.
  3. A flat **`+15` elevation** is added to reflect the fact that this AI service has not been vetted or approved by administrators:
     $$\text{RawScore} = \text{BaseScore} + \sum \text{RuleWeights} + 15$$
  4. Low-confidence dampening applies if confidence $< 0.60$:
     $$\text{AdjustedScore} = \begin{cases} \text{round}(\text{RawScore} \times 0.7) & \text{if } \text{confidence} < 0.60 \\ \text{RawScore} & \text{otherwise} \end{cases}$$
  5. Final score clamped to `100`:
     $$\text{FinalScore} = \min(100, \max(0, \text{AdjustedScore}))$$
- Reasoning first line: `"Domain has not been classified by admin (unknown AI service); risk score elevated pending review."`

---

## 4. Policy Rule Matching

Active policy rules continue to evaluate specific interaction depth signals:
- **`appliesToLabel`**: Rule applies only to specified label (or all if null).
- **`domainPattern`**: Glob pattern match on activity domain.
- **`minGenerateClicks`**: Requires `evidence.generateClickCount >= minGenerateClicks`.
- **`requiresFileUpload`**: Requires `evidence.fileUploadPresent == true`.
- **`requiresPasteEvent`**: Requires `evidence.pasteEventCount > 0`.

Rule weights (0 to 40) contribute to `RawScore` for `APPROVED` and `UNKNOWN` domains.

---

## 5. Confidence Dampening Summary

If classification confidence is `< 0.60`:
- **`BLOCKED`**: **No dampening**. Always stays 100 / `CRITICAL`.
- **`APPROVED`**: Multiplies raw score by `0.7` before capping at `49`.
- **`UNKNOWN`**: Multiplies raw score by `0.7` before clamping at `100`.
- **`NON_AI`**: Score is already 0; remains 0 / `LOW`.

---

## 6. Risk Level Boundaries

| Score Range | Risk Level | Description |
|---|---|---|
| **0 – 24** | **`LOW`** | Benign or non-AI usage; standard telemetry. |
| **25 – 49** | **`MEDIUM`** | Moderate risk; approved AI activity or minor interactions. |
| **50 – 74** | **`HIGH`** | Elevated risk; unreviewed AI interactions, file uploads, pastes. |
| **75 – 100** | **`CRITICAL`** | Severe risk; blocked AI domain usage or extreme unvetted activity. |

### Exact Boundary Test Matrix:
- Score `0` &rarr; `LOW`
- Score `24` &rarr; `LOW`
- Score `25` &rarr; `MEDIUM`
- Score `49` &rarr; `MEDIUM` (Approved AI maximum)
- Score `50` &rarr; `HIGH`
- Score `74` &rarr; `HIGH`
- Score `75` &rarr; `CRITICAL`
- Score `100` &rarr; `CRITICAL` (Blocked AI standard)

---

## 7. Explainability & Reasoning Format

Every `RiskAssessment` records an explainable `reasoning` string (max 1000 characters). The domain status **must appear as the explicit first line of the reasoning**, before listing matched rules or arithmetic:

1. **First Line**: Domain status explanation:
   - `BLOCKED`: `"Domain is on the blocked AI list set by admin."`
   - `APPROVED`: `"Domain is approved for AI use; risk capped accordingly."`
   - `UNKNOWN`: `"Domain has not been classified by admin (unknown AI service); risk score elevated pending review."`
   - `NON_AI`: `"Activity classified as non-AI; zero risk assigned."`
2. **Second Line**: Classification label, confidence percentage, and effective base score.
3. **Third Line**: Matched rules and weights.
4. **Fourth Line**: Detailed score arithmetic (including dampening and domain ceilings).
5. **Final Line**: Clamped final score and `RiskLevel`.
