package com.example.focusscrambler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;

import java.sql.SQLDataException;

public class login_page extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private CheckBox chkPass;
    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_page);

        // Apply edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the database manager
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (SQLDataException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error opening database", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Bind UI elements
        editTextEmail = findViewById(R.id.txt_email);
        editTextPassword = findViewById(R.id.txt_pass);
        Button btn_login_action = findViewById(R.id.btn_login);
        chkPass = findViewById(R.id.chk_pass);

        // Login button click
        btn_login_action.setOnClickListener(v -> loginUser());

        // Signup button click
        Button btn_signin = findViewById(R.id.btn_signup_page);
        btn_signin.setOnClickListener(v -> {
            Intent intent = new Intent(login_page.this, signup_page.class);
            startActivity(intent);
        });

        // Password reveal toggle
        chkPass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextPassword.setTransformationMethod(null);
            } else {
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            editTextPassword.setSelection(editTextPassword.getText().length());
        });
    }

    // ----------------------------------------
    // UPDATED LOGIN FUNCTION (BCrypt Compatible)
    // ----------------------------------------
    private void loginUser() {
        String emailOrUsername = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Basic validation
        if (emailOrUsername.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email/username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Try login with username first, then email
        if (attemptLoginByUsername(emailOrUsername, password)) {
            return; // Success
        }

        if (attemptLoginByEmail(emailOrUsername, password)) {
            return; // Success
        }

        // Both failed
        Toast.makeText(this, "Invalid email/username or password", Toast.LENGTH_SHORT).show();
    }

    /**
     * Attempt login using username
     */
    private boolean attemptLoginByEmail(String email, String plainPassword) {
        Cursor cursor = null;
        try {
            cursor = dbManager.fetchUserByEmail(email);

            // Check if cursor is valid and has data
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PASSWORD);
                String storedHash = cursor.getString(passwordIndex);

                int userIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.USER_ID);
                long userId = cursor.getLong(userIdIndex);

                if (PasswordUtils.verifyPassword(plainPassword, storedHash)) {
                    saveLoginState(userId);
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("LoginPage", "Login error", e);
        } finally {
            // ALWAYS close cursor in finally block
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    private boolean attemptLoginByUsername(String username, String plainPassword) {
        Cursor cursor = null;
        try {
            cursor = dbManager.fetchUserByUsername(username);

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                int passwordIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.USER_PASSWORD);
                String storedHash = cursor.getString(passwordIndex);

                int userIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.USER_ID);
                long userId = cursor.getLong(userIdIndex);

                if (PasswordUtils.verifyPassword(plainPassword, storedHash)) {
                    saveLoginState(userId);
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                    return true;
                }
            }
        } catch (Exception e) {
            Log.e("LoginPage", "Login error", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    /**
     * Save logged-in user ID for session management
     */
    private void saveLoginState(long userId) {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("current_user_id", userId);
        editor.apply();
    }

    /**
     * Navigate to dashboard with clear task stack
     */
    private void goToDashboard() {
        Intent intent = new Intent(login_page.this, dashboard_withnav_page.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}