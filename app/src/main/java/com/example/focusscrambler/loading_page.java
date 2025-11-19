package com.example.focusscrambler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class loading_page extends AppCompatActivity {

    // Define the delay time in milliseconds (e.g., 3000ms = 3 seconds)
    private static final int LOADING_TIMEOUT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading_page);

        // This part is for edge-to-edge display, which is fine to keep.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Use a Handler to delay the start of the login_page activity
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Create an Intent to start the login_page activity
                Intent intent = new Intent(loading_page.this, login_page.class);
                startActivity(intent);

                // Finish the loading_page activity so the user can't go back to it
                finish();
            }
        }, LOADING_TIMEOUT);
    }
}
