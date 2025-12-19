package com.example.focusscrambler; // Use your actual package name

public class CalendarDate {
    private String dayOfWeek;
    private int dateNumber;

    public CalendarDate(String dayOfWeek, int dateNumber) {
        this.dayOfWeek = dayOfWeek;
        this.dateNumber = dateNumber;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public int getDateNumber() {
        return dateNumber;
    }
}