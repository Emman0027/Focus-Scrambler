package com.example.focusscrambler;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.SQLDataException;

public class signup_page extends AppCompatActivity {

    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    private Button btn_signup;
    private Button btn_signin_page;

    private DatabaseManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_page);

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init DB
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (SQLDataException e) {
            throw new RuntimeException(e);
        }

        // Bind UI fields
        editTextFirstName = findViewById(R.id.txt_fname);
        editTextLastName = findViewById(R.id.txt_lname);
        editTextUsername = findViewById(R.id.txt_user);
        editTextEmail = findViewById(R.id.txt_email);
        editTextPassword = findViewById(R.id.txt_pass);
        editTextConfirmPassword = findViewById(R.id.txt_confirmpass);

        btn_signup = findViewById(R.id.btn_signup);
        btn_signin_page = findViewById(R.id.btn_signup_page);



        // ✔ SIGNUP BUTTON (with password confirmation)
        btn_signup.setOnClickListener(v -> {

            String fname = editTextFirstName.getText().toString().trim();
            String lname = editTextLastName.getText().toString().trim();
            String username = editTextUsername.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confPassword = editTextConfirmPassword.getText().toString().trim();

            // Check empty fields
            if (fname.isEmpty() || lname.isEmpty() || username.isEmpty() ||
                    email.isEmpty() || password.isEmpty() || confPassword.isEmpty()) {

                Toast.makeText(signup_page.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check password match
            if (!password.equals(confPassword)) {
                Toast.makeText(signup_page.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert into DB
            dbManager.insertQuery(fname, lname, username, email, password);

            Toast.makeText(signup_page.this, "Sign up successful!", Toast.LENGTH_SHORT).show();

            // Go to login page
            Intent intent = new Intent(signup_page.this, login_page.class);
            startActivity(intent);
            finish();
        });

        // ✔ BUTTON TO GO TO LOGIN PAGE
        btn_signin_page.setOnClickListener(v -> {
            Intent intent = new Intent(signup_page.this, login_page.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.close();
    }
}
