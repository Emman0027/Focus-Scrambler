package com.example.focusscrambler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "FocusScrambler.db";
    static final int DATABASE_VERSION = 2; // Increment this when you change schema

    // USERS Table
    static final String TABLE_USERS = "users"; // Renamed for common convention
    static final String USER_ID = "_id"; // Primary Key for users
    static final String USER_FIRST_NAME = "first_name";
    static final String USER_LAST_NAME = "last_name";
    static final String USER_USERNAME = "username";
    static final String USER_EMAIL = "email";
    static final String USER_PASSWORD = "password"; // Consider hashing passwords!

    // TASKS Table
    static final String TABLE_TASKS = "tasks"; // New table for tasks
    static final String TASK_ID = "_id"; // Primary Key for tasks
    static final String TASK_USER_ID = "user_id"; // Foreign Key linking to USERS table
    static final String TASK_TITLE = "title";
    static final String TASK_DESCRIPTION = "description";
    static final String TASK_DUE_DATE = "due_date"; // Stored as TEXT or INTEGER (Unix timestamp)
    static final String TASK_STATUS = "status"; // e.g., 'pending', 'completed'
    static final String TASK_CREATED_AT = "created_at"; // Timestamp of creation

    // Create USERS table query
    static final String CREATE_USERS_TABLE_QUERY = "CREATE TABLE " + TABLE_USERS +
            "(" + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            USER_FIRST_NAME + " TEXT, " +
            USER_LAST_NAME + " TEXT, " +
            USER_USERNAME + " TEXT NOT NULL UNIQUE, " + // Username should be unique
            USER_EMAIL + " TEXT, " +
            USER_PASSWORD + " TEXT NOT NULL);"; // Password should be NOT NULL

    // Create TASKS table query
    static final String CREATE_TASKS_TABLE_QUERY = "CREATE TABLE " + TABLE_TASKS +
            "(" + TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TASK_USER_ID + " INTEGER NOT NULL, " +
            TASK_TITLE + " TEXT NOT NULL, " +
            TASK_DESCRIPTION + " TEXT, " +
            TASK_DUE_DATE + " INTEGER, " + // Storing date as Unix timestamp (long)
            TASK_STATUS + " TEXT NOT NULL DEFAULT 'pending', " + // Default status
            TASK_CREATED_AT + " INTEGER NOT NULL, " + // Store creation timestamp
            "FOREIGN KEY(" + TASK_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE);"; // Foreign key constraint

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE_QUERY);
        db.execSQL(CREATE_TASKS_TABLE_QUERY); // Create the tasks table
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This method is called when DATABASE_VERSION is incremented
        // It's crucial for schema migration. For development, dropping and recreating is common.
        // For production, you'd write ALTER TABLE statements to preserve data.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS); // Drop tasks table first
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS); // Then drop users table
        onCreate(db); // Recreate all tables
    }
}