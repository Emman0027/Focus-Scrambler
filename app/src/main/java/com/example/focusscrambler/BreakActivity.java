package com.example.focusscrambler;

public class BreakActivity {
    private String description;  // Task name (e.g., "Do 10 pushups")
    private int durationSeconds; // Break duration in SECONDS (0-300 seconds)
    private long id;             // Database primary key (-1 = not saved)
    private boolean isCompleted; // NEW: Track if activity is done

    // ✅ Constructor for new activities (not yet in DB)
    public BreakActivity(String description) {
        this(description, 30, -1L, false); // Default 30 seconds, invalid ID, not completed
    }

    // ✅ Constructor for database-loaded activities
    public BreakActivity(String description, int durationSeconds, long id, boolean isCompleted) {
        this.description = description;
        this.durationSeconds = durationSeconds;
        this.id = id;
        this.isCompleted = isCompleted;
    }

    // ✅ Constructor for display (from previous code)
    public BreakActivity(String description, int durationMinutes, long id) {
        this(description, durationMinutes * 60, id, false); // Convert minutes to seconds
    }

    // ✅ Getters
    public String getDescription() {
        return description;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getDurationMinutes() {
        return durationSeconds / 60;
    }

    public long getId() {
        return id;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    // ✅ Setters (for editing)
    public void setDescription(String description) {
        this.description = description;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationSeconds = durationMinutes * 60;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    // ✅ Check if saved to database
    public boolean isSaved() {
        return id != -1L;
    }
}