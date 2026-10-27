package com.lifelink.request.model.enums;

/**
 * Coordination urgency levels with SLA thresholds for follow-up notifications.
 */
public enum RequestUrgency {
    ROUTINE("Routine", 240),
    URGENT("Urgent", 60),
    CRITICAL("Critical", 15);

    private final String label;
    private final int defaultEscalationMinutes;

    RequestUrgency(String label, int defaultEscalationMinutes) {
        this.label = label;
        this.defaultEscalationMinutes = defaultEscalationMinutes;
    }

    public String getLabel() {
        return label;
    }

    public int getDefaultEscalationMinutes() {
        return defaultEscalationMinutes;
    }
}
