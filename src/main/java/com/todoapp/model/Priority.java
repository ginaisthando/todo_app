package com.todoapp.model;

public enum Priority {
    LOW("Low", 1, "#28a745"),
    MEDIUM("Medium", 2, "#ffc107"),
    HIGH("High", 3, "#fd7e14"),
    URGENT("Urgent", 4, "#dc3545");

    private final String displayName;
    private final int level;
    private final String color;

    Priority(String displayName, int level, String color) {
        this.displayName = displayName;
        this.level = level;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Priority fromLevel(int level) {
        for (Priority priority : Priority.values()) {
            if (priority.level == level) {
                return priority;
            }
        }
        return MEDIUM; // Default
    }

    public static Priority fromDisplayName(String displayName) {
        for (Priority priority : Priority.values()) {
            if (priority.displayName.equalsIgnoreCase(displayName)) {
                return priority;
            }
        }
        return MEDIUM; // Default
    }
}
