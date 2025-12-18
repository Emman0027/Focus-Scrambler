package com.example.focusscrambler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLDataException;

public class DatabaseManager {
    private DatabaseHelper dbHelper;
    private Context context;
    private SQLiteDatabase database;

    public DatabaseManager(Context ctx) {
        context = ctx;
    }

    public DatabaseManager open() throws SQLDataException {
        dbHelper = new DatabaseHelper(context);
        this.database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        if (database != null) {
            dbHelper.close();
        }
    }

    public void insertQuery(String firstname, String lastname, String username, String email, String password){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.USER_FIRST_NAME, firstname);
        contentValues.put(DatabaseHelper.USER_LAST_NAME, lastname);
        contentValues.put(DatabaseHelper.USER_USERNAME, username);
        contentValues.put(DatabaseHelper.USER_EMAIL, email);
        contentValues.put(DatabaseHelper.USER_PASSWORD, password);
        database.insert(DatabaseHelper.DATABASE_TABLE, null, contentValues);
    }

    public Cursor fetch(){
        String[] columns = new String[] {DatabaseHelper.USER_ID, DatabaseHelper.USER_FIRST_NAME, DatabaseHelper.USER_LAST_NAME, DatabaseHelper.USER_USERNAME, DatabaseHelper.USER_EMAIL, DatabaseHelper.USER_PASSWORD};
        Cursor cursor = database.query(DatabaseHelper.DATABASE_TABLE, columns, null, null, null, null, null);
        if (cursor != null){
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchUserByEmail(String email) {
        // Define the columns you want to retrieve
        String[] columns = new String[]{
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_EMAIL,
                DatabaseHelper.USER_PASSWORD // Important: We need the password to compare it
                // Add other columns like USER_FIRST_NAME if you need them
        };

        // Define the selection criteria (WHERE clause)
        // The '?' is a placeholder to prevent SQL injection.
        String selection = DatabaseHelper.USER_EMAIL + " = ?";

        // Define the arguments for the placeholder
        String[] selectionArgs = new String[]{email};

        // Execute the query
        // The query will be: SELECT _id, email, password FROM users WHERE email = ?
        Cursor cursor = database.query(
                DatabaseHelper.DATABASE_TABLE, // Table name
                columns,                       // Columns to return
                selection,                     // The WHERE clause
                selectionArgs,                 // The values for the WHERE clause
                null,                          // groupBy
                null,                          // having
                null                           // orderBy
        );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int update(long _id, String username, String firstname, String lastname, String email, String password) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.USER_FIRST_NAME, firstname);
        contentValues.put(DatabaseHelper.USER_LAST_NAME, lastname);
        contentValues.put(DatabaseHelper.USER_USERNAME, username);
        contentValues.put(DatabaseHelper.USER_EMAIL, email);
        contentValues.put(DatabaseHelper.USER_PASSWORD, password);
        int ret = database.update(DatabaseHelper.DATABASE_TABLE, contentValues, DatabaseHelper.USER_ID + " = " + _id, null);
        return ret;

    }

    public void delete(long _id) {
        database.delete(DatabaseHelper.DATABASE_TABLE, DatabaseHelper.USER_ID + "=" + _id, null);
        }
}