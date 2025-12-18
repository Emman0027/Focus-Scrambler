package com.example.focusscrambler;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
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
        chkPass = findViewById(R.id.chk_pass);  // ✅ Password reveal checkbox

        // Login button click
        btn_login_action.setOnClickListener(v -> loginUser());

        // Signup button click
        Button btn_signin = findViewById(R.id.btn_signup_page);
        btn_signin.setOnClickListener(v -> {
            Intent intent = new Intent(login_page.this, signup_page.class);
            startActivity(intent);
        });

        // ✅ Password reveal toggle
        chkPass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                editTextPassword.setTransformationMethod(null);
            } else {
                // Hide password
                editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // Move cursor to end after transformation
            editTextPassword.setSelection(editTextPassword.getText().length());
        });
    }

    // ----------------------------------------
    // LOGIN FUNCTION
    // ----------------------------------------
    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch user by email
        Cursor cursor = dbManager.fetchUserByEmail(email);

        // Check if email exists
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, "Email account does not exist", Toast.LENGTH_SHORT).show();
            if (cursor != null) cursor.close();
            return;
        }

        cursor.moveToFirst();
        String storedPassword = cursor.getString(2);
        cursor.close();

        // Compare passwords
        if (password.equals(storedPassword)) {
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(login_page.this, dashboard_withnav_page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        } else {
            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}