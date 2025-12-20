package com.example.focusscrambler;

import java.util.Locale;

public abstract class HistoryItem {
    public static final int TYPE_DATE_HEADER = 0;
    public static final int TYPE_ACTIVITY = 1;

    public abstract int getType();

    public static class DateHeader extends HistoryItem {
        private String dateString;

        public DateHeader(String dateString) {
            this.dateString = dateString;
        }

        public String getDateString() {
            return dateString;
        }

        @Override
        public int getType() {
            return TYPE_DATE_HEADER;
        }
    }

    public static class ActivityEntry extends HistoryItem {
        private String description;
        private long durationMillis;
        private boolean isFocusSession;
        private long timestamp;

        public ActivityEntry(String description, long durationMillis, boolean isFocusSession, long timestamp) {
            this.description = description;
            this.durationMillis = durationMillis;
            this.isFocusSession = isFocusSession;
            this.timestamp = timestamp;
        }

        public String getDescription() {
            return description;
        }

        public long getDurationMillis() {
            return durationMillis;
        }

        public boolean isFocusSession() {
            return isFocusSession;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public String getFormattedDuration() {
            long totalSeconds = durationMillis / 1000;
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;

            if (minutes > 0) {
                if (seconds > 0) {
                    return String.format(Locale.getDefault(), "%dmin %02ds", minutes, seconds);
                } else {
                    return String.format(Locale.getDefault(), "%dmin", minutes);
                }
            } else {
                return String.format(Locale.getDefault(), "%ds", seconds);
            }
        }

        @Override
        public int getType() {
            return TYPE_ACTIVITY;
        }
    }
}