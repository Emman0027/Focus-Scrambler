package com.example.focusscrambler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "FocusScrambler.db";
    static final int DATABASE_VERSION = 4; // ✅ INCREMENTED to 4 for HISTORY table

    // USERS Table
    static final String TABLE_USERS = "users";
    static final String USER_ID = "_id";
    static final String USER_FIRST_NAME = "first_name";
    static final String USER_LAST_NAME = "last_name";
    static final String USER_USERNAME = "username";
    static final String USER_EMAIL = "email";
    static final String USER_PASSWORD = "password";

    // TASKS Table
    static final String TABLE_TASKS = "tasks";
    static final String TASK_ID = "_id";
    static final String TASK_USER_ID = "user_id";
    static final String TASK_TITLE = "title";
    static final String TASK_DESCRIPTION = "description";
    static final String TASK_DUE_DATE = "due_date";
    static final String TASK_STATUS = "status";
    static final String TASK_CREATED_AT = "created_at";

    // BREAK_ACTIVITIES Table
    static final String TABLE_BREAK_ACTIVITIES = "break_activities";
    static final String BREAK_ID = "_id";
    static final String BREAK_USER_ID = "user_id";
    static final String BREAK_DATE = "date";
    static final String BREAK_TASK = "task";
    static final String BREAK_DURATION = "duration_minutes";

    // ✅ NEW: HISTORY Table for completed break activities
    static final String TABLE_HISTORY = "history";
    static final String HISTORY_ID = "history_id";
    static final String HISTORY_USER_ID = "user_id";
    static final String HISTORY_BREAK_ID = "break_activity_id";
    static final String HISTORY_ACTIVITY_NAME = "activity_name";
    static final String HISTORY_DURATION_MINUTES = "duration_minutes";
    static final String HISTORY_COMPLETION_DATE = "completion_date";
    static final String HISTORY_COMPLETION_TIME = "completion_time";
    static final String HISTORY_SESSION_NUMBER = "session_number";

    // Create table queries
    static final String CREATE_USERS_TABLE_QUERY = "CREATE TABLE " + TABLE_USERS +
            "(" + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            USER_FIRST_NAME + " TEXT, " +
            USER_LAST_NAME + " TEXT, " +
            USER_USERNAME + " TEXT NOT NULL UNIQUE, " +
            USER_EMAIL + " TEXT, " +
            USER_PASSWORD + " TEXT NOT NULL);";

    static final String CREATE_TASKS_TABLE_QUERY = "CREATE TABLE " + TABLE_TASKS +
            "(" + TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TASK_USER_ID + " INTEGER NOT NULL, " +
            TASK_TITLE + " TEXT NOT NULL, " +
            TASK_DESCRIPTION + " TEXT, " +
            TASK_DUE_DATE + " INTEGER, " +
            TASK_STATUS + " TEXT NOT NULL DEFAULT 'pending', " +
            TASK_CREATED_AT + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + TASK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE);";

    static final String CREATE_BREAK_ACTIVITIES_TABLE_QUERY = "CREATE TABLE " + TABLE_BREAK_ACTIVITIES +
            "(" + BREAK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BREAK_USER_ID + " INTEGER NOT NULL, " +
            BREAK_DATE + " TEXT NOT NULL, " +
            BREAK_TASK + " TEXT NOT NULL, " +
            BREAK_DURATION + " INTEGER NOT NULL, " +
            "FOREIGN KEY(" + BREAK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE);";

    // ✅ NEW: Create HISTORY table query
    static final String CREATE_HISTORY_TABLE_QUERY = "CREATE TABLE " + TABLE_HISTORY +
            "(" + HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            HISTORY_USER_ID + " INTEGER NOT NULL, " +
            HISTORY_BREAK_ID + " INTEGER, " +
            HISTORY_ACTIVITY_NAME + " TEXT NOT NULL, " +
            HISTORY_DURATION_MINUTES + " INTEGER NOT NULL, " +
            HISTORY_COMPLETION_DATE + " TEXT NOT NULL, " +
            HISTORY_COMPLETION_TIME + " TEXT NOT NULL, " +
            HISTORY_SESSION_NUMBER + " INTEGER, " +
            "FOREIGN KEY(" + HISTORY_BREAK_ID + ") REFERENCES " + TABLE_BREAK_ACTIVITIES + "(" + BREAK_ID + "), " +
            "FOREIGN KEY(" + HISTORY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE_QUERY);
        db.execSQL(CREATE_TASKS_TABLE_QUERY);
        db.execSQL(CREATE_BREAK_ACTIVITIES_TABLE_QUERY);
        db.execSQL(CREATE_HISTORY_TABLE_QUERY); // ✅ NEW TABLE
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // ✅ Proper migration handling for different versions
        if (oldVersion < 4) {
            // Version 3 -> 4: Add HISTORY table (no data loss)
            db.execSQL(CREATE_HISTORY_TABLE_QUERY);
        }

        // Future versions can add more specific migrations here
        // if (oldVersion < 5) { ... }

        // ⚠️ TEMPORARY: For development - drops ALL tables and recreates
        // Remove this when app goes to production!
        /*
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BREAK_ACTIVITIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        */
    }
}