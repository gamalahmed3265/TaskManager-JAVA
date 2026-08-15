package com.taskmanager.model;

public enum TaskStatus {
    PENDING("قيد الانتظار"),
    IN_PROGRESS("قيد التنفيذ"),
    COMPLETED("مكتملة"),
    POSTPONED("مؤجلة"),
    OVERDUE("متأخرة");

    private final String displayName;

    TaskStatus(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}
