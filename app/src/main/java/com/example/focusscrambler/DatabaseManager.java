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

    // ✅ UPDATED: Now hashes password before storing
    public void insertQuery(String firstname, String lastname, String username, String email, String plainPassword) {
        String hashedPassword = PasswordUtils.hashPassword(plainPassword); // Hash first!

        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.USER_FIRST_NAME, firstname);
        contentValues.put(DatabaseHelper.USER_LAST_NAME, lastname);
        contentValues.put(DatabaseHelper.USER_USERNAME, username);
        contentValues.put(DatabaseHelper.USER_EMAIL, email);
        contentValues.put(DatabaseHelper.USER_PASSWORD, hashedPassword); // Store HASH

        database.insert(DatabaseHelper.TABLE_USERS, null, contentValues);
    }

    // ✅ UPDATED: Use correct table name and exclude password from fetch for security
    public Cursor fetch() {
        String[] columns = new String[] {
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_FIRST_NAME,
                DatabaseHelper.USER_LAST_NAME,
                DatabaseHelper.USER_USERNAME,
                DatabaseHelper.USER_EMAIL
                // Excluded USER_PASSWORD for security
        };
        Cursor cursor = database.query(DatabaseHelper.TABLE_USERS, columns, null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor fetchUserById(long userId) {
        String[] columns = new String[]{
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_FIRST_NAME,
                DatabaseHelper.USER_LAST_NAME,
                DatabaseHelper.USER_USERNAME,
                DatabaseHelper.USER_EMAIL
        };

        String selection = DatabaseHelper.USER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(userId)};

        return database.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);
    }

    // ✅ UPDATED: Use correct table name
    public Cursor fetchUserByEmail(String email) {
        String[] columns = new String[]{
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_EMAIL,
                DatabaseHelper.USER_PASSWORD
        };

        String selection = DatabaseHelper.USER_EMAIL + " = ?";
        String[] selectionArgs = new String[]{email};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS, // ✅ Fixed table name
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    // ✅ NEW: Fetch by username (for login)
    public Cursor fetchUserByUsername(String username) {
        String[] columns = new String[]{
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_USERNAME,
                DatabaseHelper.USER_PASSWORD,
                DatabaseHelper.USER_EMAIL
        };

        String selection = DatabaseHelper.USER_USERNAME + " = ?";
        String[] selectionArgs = new String[]{username};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    // ✅ NEW: Fetch by firstname
    public Cursor fetchUserByFirstName(String firstname) {
        String[] columns = new String[]{
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_FIRST_NAME,
                DatabaseHelper.USER_LAST_NAME,
                DatabaseHelper.USER_USERNAME
        };

        String selection = DatabaseHelper.USER_FIRST_NAME + " = ?";
        String[] selectionArgs = new String[]{firstname};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    // ✅ NEW: Fetch by lastname
    public Cursor fetchUserByLastName(String lastname) {
        String[] columns = new String[]{
                DatabaseHelper.USER_ID,
                DatabaseHelper.USER_FIRST_NAME,
                DatabaseHelper.USER_LAST_NAME,
                DatabaseHelper.USER_USERNAME
        };

        String selection = DatabaseHelper.USER_LAST_NAME + " = ?";
        String[] selectionArgs = new String[]{lastname};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    // ✅ FIXED: SQL injection vulnerability + correct table name
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

        int ret = database.update(DatabaseHelper.TABLE_USERS, contentValues, selection, selectionArgs);
        return ret;
    }

    // ✅ FIXED: SQL injection vulnerability + correct table name
    public void delete(long userId) {
        String selection = DatabaseHelper.USER_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(userId)};
        database.delete(DatabaseHelper.TABLE_USERS, selection, selectionArgs);
    }

    // ✅ NEW: Login method using BCrypt verification
    public boolean authenticateUser(String username, String plainPassword) {
        Cursor cursor = fetchUserByUsername(username);
        if (cursor != null && cursor.moveToFirst()) {
            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PASSWORD));
            cursor.close();
            return PasswordUtils.verifyPassword(plainPassword, storedHash);
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }
}