package com.example.focusscrambler; // Adjust package name

public class BreakActivity {
    private String description;
    private int id; // Optional, for unique identification

    public BreakActivity(String description) {
        this.description = description;
        // Optionally generate a unique ID
        this.id = description.hashCode(); // Simple hash for demo
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }
}