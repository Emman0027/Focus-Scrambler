package com.example.focusscrambler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "FocusScrambler.db";
    static final int DATABASE_VERSION = 1;

    static final String DATABASE_TABLE = "USERS";
    static final String USER_ID = "_ID";
    static final String USER_FIRST_NAME = "FIRST_NAME";
    static final String USER_LAST_NAME = "LAST_NAME";
    static final String USER_USERNAME = "USERNAME";
    static final String USER_EMAIL = "EMAIL";
    static final String USER_PASSWORD = "PASSWORD";
    static final String CREATE_DB_QUERY = "CREATE TABLE " + DATABASE_TABLE +
            "(" + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            USER_FIRST_NAME + " TEXT, " +
            USER_LAST_NAME + " TEXT, " +
            USER_USERNAME + " TEXT, " +
            USER_EMAIL + " TEXT, " +
            USER_PASSWORD + " TEXT);";



    public DatabaseHelper(Context context) {
        super(context, DatabaseHelper.DATABASE_NAME, null, DatabaseHelper. DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }
}
