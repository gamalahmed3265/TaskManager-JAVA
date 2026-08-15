package com.taskmanager.model;

/**
 * أولوية المهمة: عالية، متوسطة، منخفضة
 */
public enum Priority {
    HIGH("عالية", 3, "#e74c3c"),
    MEDIUM("متوسطة", 2, "#f39c12"),
    LOW("منخفضة", 1, "#27ae60");

    private final String displayName;
    private final int weight;
    private final String colorHex;

    Priority(String displayName, int weight, String colorHex) {
        this.displayName = displayName;
        this.weight = weight;
        this.colorHex = colorHex;
    }

    public String getDisplayName() { return displayName; }
    public int getWeight() { return weight; }
    public String getColorHex() { return colorHex; }

    @Override
    public String toString() { return displayName; }
}
