package com.example.focusscrambler; // Use your actual package name

public class CalendarDate {
    private String dayOfWeek;
    private int dateNumber;
    private boolean isToday;      // New: Flag for today's date
    private boolean isSelected;   // New: Flag for selected date

    public CalendarDate(String dayOfWeek, int dateNumber, boolean isToday) {
        this.dayOfWeek = dayOfWeek;
        this.dateNumber = dateNumber;
        this.isToday = isToday;
        this.isSelected = false; // Default to not selected
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public int getDateNumber() {
        return dateNumber;
    }

    public boolean isToday() {
        return isToday;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}