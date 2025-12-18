package com.example.focusscrambler;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class front_page extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_front_page);

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create button - opens signup_page
        Button btnCreate = findViewById(R.id.btn_create);
        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(front_page.this, signup_page.class);
            startActivity(intent);
        });

        // Login button - opens login_page
        Button btnLogin = findViewById(R.id.btn_login_2);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(front_page.this, login_page.class);
            startActivity(intent);
        });
    }
}