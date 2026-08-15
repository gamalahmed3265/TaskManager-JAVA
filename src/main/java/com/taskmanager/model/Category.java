package com.taskmanager.model;

/**
 * تصنيف المهمة
 */
public enum Category {
    WORK("عمل", "#3498db"),
    PERSONAL("شخصي", "#9b59b6"),
    STUDY("دراسة", "#1abc9c"),
    HEALTH("صحة", "#e67e22"),
    OTHER("أخرى", "#7f8c8d");

    private final String displayName;
    private final String colorHex;

    Category(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() { return displayName; }
    public String getColorHex() { return colorHex; }

    @Override
    public String toString() { return displayName; }
}
