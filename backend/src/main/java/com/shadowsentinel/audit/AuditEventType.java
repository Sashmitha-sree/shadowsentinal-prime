package com.shadowsentinel.audit;

public enum AuditEventType {
    LOGIN,
    LOGIN_FAILED,
    SESSION_STARTED,
    EVIDENCE_INGESTED,
    CLASSIFICATION_CREATED,
    RISK_ASSESSED,
    ALERT_CREATED,
    ALERT_ACKNOWLEDGED,
    POLICY_CHANGED
}
