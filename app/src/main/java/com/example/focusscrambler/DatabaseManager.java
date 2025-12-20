package com.example.focusscrambler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.sql.SQLDataException;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private Context context;
    public SQLiteDatabase database;

    public DatabaseManager(Context ctx) {
        context = ctx;
    }

    public DatabaseManager open() throws SQLDataException {
        dbHelper = new DatabaseHelper(context);
        this.database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (database != null) {
            dbHelper.close();
        }
    }

    // USERS METHODS (EXISTING - UNCHANGED)
    public void insertQuery(String firstname, String lastname, String username, String email, String plainPassword) {
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.USER_FIRST_NAME, firstname);
        contentValues.put(DatabaseHelper.USER_LAST_NAME, lastname);
        contentValues.put(DatabaseHelper.USER_USERNAME, username);
        contentValues.put(DatabaseHelper.USER_EMAIL, email);
        contentValues.put(DatabaseHelper.USER_PASSWORD, hashedPassword);
        database.insert(DatabaseHelper.TABLE_USERS, null, contentValues);
    }

    public Cursor fetch() {
        String[] columns = new String[] {
                DatabaseHelper.USER_ID, DatabaseHelper.USER_FIRST_NAME,
                DatabaseHelper.USER_LAST_NAME, DatabaseHelper.USER_USERNAME,
                DatabaseHelper.USER_EMAIL
        };
        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS, columns, null, null, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        return cursor;
    }

    public Cursor fetchUserById(long userId) {
        String[] columns = new String[]{
                DatabaseHelper.USER_ID, DatabaseHelper.USER_FIRST_NAME,
                DatabaseHelper.USER_LAST_NAME, DatabaseHelper.USER_USERNAME,
                DatabaseHelper.USER_EMAIL
        };
        String selection = DatabaseHelper.USER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(userId)};
        return database.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
    }

    public Cursor fetchUserByEmail(String email) {
        String[] columns = new String[]{DatabaseHelper.USER_ID, DatabaseHelper.USER_EMAIL, DatabaseHelper.USER_PASSWORD};
        String selection = DatabaseHelper.USER_EMAIL + " = ?";
        String[] selectionArgs = new String[]{email};
        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        return cursor;
    }

    public Cursor fetchUserByUsername(String username) {
        String[] columns = new String[]{DatabaseHelper.USER_ID, DatabaseHelper.USER_USERNAME, DatabaseHelper.USER_PASSWORD, DatabaseHelper.USER_EMAIL};
        String selection = DatabaseHelper.USER_USERNAME + " = ?";
        String[] selectionArgs = new String[]{username};
        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        return cursor;
    }

    public int update(long userId, String username, String firstname, String lastname, String email, String plainPassword) {
        String hashedPassword = PasswordUtils.hashPassword(plainPassword);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.USER_FIRST_NAME, firstname);
        contentValues.put(DatabaseHelper.USER_LAST_NAME, lastname);
        contentValues.put(DatabaseHelper.USER_USERNAME, username);
        contentValues.put(DatabaseHelper.USER_EMAIL, email);
        contentValues.put(DatabaseHelper.USER_PASSWORD, hashedPassword);
        String selection = DatabaseHelper.USER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(userId)};
        return database.update(DatabaseHelper.TABLE_USERS, contentValues, selection, selectionArgs);
    }

    public void delete(long userId) {
        String selection = DatabaseHelper.USER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(userId)};
        database.delete(DatabaseHelper.TABLE_USERS, selection, selectionArgs);
    }

    public boolean authenticateUser(String username, String plainPassword) {
        Cursor cursor = fetchUserByUsername(username);
        if (cursor != null && cursor.moveToFirst()) {
            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PASSWORD));
            cursor.close();
            return PasswordUtils.verifyPassword(plainPassword, storedHash);
        }
        if (cursor != null) cursor.close();
        return false;
    }

    // BREAK_ACTIVITIES METHODS
    /**
     * Insert new break activity for user
     * @return new break activity ID (or -1 if failed)
     */
    public long insertBreakActivity(long userId, String date, String task, int durationSeconds) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.BREAK_USER_ID, userId);
        values.put(DatabaseHelper.BREAK_DATE, date);              // "2025-12-20"
        values.put(DatabaseHelper.BREAK_TASK, task);              // "Do 10 pushups"
        values.put(DatabaseHelper.BREAK_DURATION, durationSeconds); // 30 (seconds)

        return database.insert(DatabaseHelper.TABLE_BREAK_ACTIVITIES, null, values);
    }

    /**
     * Get all break activities for a specific user
     */
    public Cursor getBreakActivitiesForUser(long userId) {
        String[] columns = {
                DatabaseHelper.BREAK_ID,
                DatabaseHelper.BREAK_TASK,
                DatabaseHelper.BREAK_DURATION,
                DatabaseHelper.BREAK_DATE
        };

        String selection = DatabaseHelper.BREAK_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        String orderBy = DatabaseHelper.BREAK_ID + " DESC"; // Newest first

        return database.query(
                DatabaseHelper.TABLE_BREAK_ACTIVITIES,
                columns,
                selection,
                selectionArgs,
                null, null, orderBy
        );
    }

    /**
     * Get break activities for user by date (for history) - this method might not be directly used
     * if all history is pulled from the HISTORY table directly.
     */
    public Cursor getBreakActivitiesByDate(long userId, String date) {
        String[] columns = {
                DatabaseHelper.BREAK_ID,
                DatabaseHelper.BREAK_TASK,
                DatabaseHelper.BREAK_DURATION
        };

        String selection = DatabaseHelper.BREAK_USER_ID + " = ? AND " +
                DatabaseHelper.BREAK_DATE + " = ?";
        String[] selectionArgs = {String.valueOf(userId), date};

        return database.query(
                DatabaseHelper.TABLE_BREAK_ACTIVITIES,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );
    }

    /**
     * Delete specific break activity
     */
    public int deleteBreakActivity(long breakId) {
        String selection = DatabaseHelper.BREAK_ID + " = ?";
        String[] selectionArgs = {String.valueOf(breakId)};
        return database.delete(DatabaseHelper.TABLE_BREAK_ACTIVITIES, selection, selectionArgs);
    }

    /**
     * Get break activity count for user (for statistics)
     */
    public int getBreakActivityCount(long userId) {
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_BREAK_ACTIVITIES +
                " WHERE " + DatabaseHelper.BREAK_USER_ID + " = ?";
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return count;
    }

    // HISTORY METHODS
    /**
     * Insert completed break activity into history.
     * @return new history ID (or -1 if failed)
     */
    public long insertHistoryEntry(long userId, long breakActivityId, String activityName,
                                   int durationMinutes, String completionDate, String completionTime, int sessionNumber) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.HISTORY_USER_ID, userId);
        // HISTORY_BREAK_ID can be null if it's a default activity not from BREAK_ACTIVITIES table
        if (breakActivityId != -1) {
            values.put(DatabaseHelper.HISTORY_BREAK_ID, breakActivityId);
        } else {
            values.putNull(DatabaseHelper.HISTORY_BREAK_ID);
        }
        values.put(DatabaseHelper.HISTORY_ACTIVITY_NAME, activityName);
        values.put(DatabaseHelper.HISTORY_DURATION_MINUTES, durationMinutes);
        values.put(DatabaseHelper.HISTORY_COMPLETION_DATE, completionDate);
        values.put(DatabaseHelper.HISTORY_COMPLETION_TIME, completionTime);
        values.put(DatabaseHelper.HISTORY_SESSION_NUMBER, sessionNumber);

        return database.insert(DatabaseHelper.TABLE_HISTORY, null, values);
    }

    /**
     * Get history entries for a specific user.
     */
    public Cursor getHistoryEntriesForUser(long userId) {
        String[] columns = {
                DatabaseHelper.HISTORY_ID,
                DatabaseHelper.HISTORY_ACTIVITY_NAME,
                DatabaseHelper.HISTORY_DURATION_MINUTES,
                DatabaseHelper.HISTORY_COMPLETION_DATE,
                DatabaseHelper.HISTORY_COMPLETION_TIME,
                DatabaseHelper.HISTORY_SESSION_NUMBER
        };

        String selection = DatabaseHelper.HISTORY_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};
        // Order by date and time descending to show newest first
        String orderBy = DatabaseHelper.HISTORY_COMPLETION_DATE + " DESC, " +
                DatabaseHelper.HISTORY_COMPLETION_TIME + " DESC";

        return database.query(
                DatabaseHelper.TABLE_HISTORY,
                columns,
                selection,
                selectionArgs,
                null, null, orderBy
        );
    }

    /**
     * Deletes a history entry by its history ID.
     * @param historyId The ID of the history entry to delete.
     * @return The number of rows affected (1 if successful, 0 otherwise).
     */
    public int deleteHistoryEntry(long historyId) {
        String selection = DatabaseHelper.HISTORY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(historyId)};
        return database.delete(DatabaseHelper.TABLE_HISTORY, selection, selectionArgs);
    }


    // BONUS: TASKS METHODS (for future use)
    public long insertTask(long userId, String title, String description, long dueDate, String status) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TASK_USER_ID, userId);
        values.put(DatabaseHelper.TASK_TITLE, title);
        values.put(DatabaseHelper.TASK_DESCRIPTION, description);
        values.put(DatabaseHelper.TASK_DUE_DATE, dueDate);
        values.put(DatabaseHelper.TASK_STATUS, status);
        values.put(DatabaseHelper.TASK_CREATED_AT, System.currentTimeMillis());

        return database.insert(DatabaseHelper.TABLE_TASKS, null, values);
    }

    public Cursor getTasksForUser(long userId) {
        String[] columns = {
                DatabaseHelper.TASK_ID, DatabaseHelper.TASK_TITLE,
                DatabaseHelper.TASK_STATUS, DatabaseHelper.TASK_CREATED_AT
        };
        String selection = DatabaseHelper.TASK_USER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(userId)};

        return database.query(
                DatabaseHelper.TABLE_TASKS, columns, selection, selectionArgs,
                null, null, DatabaseHelper.TASK_CREATED_AT + " DESC"
        );
    }
}