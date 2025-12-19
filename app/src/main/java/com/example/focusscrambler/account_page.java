package com.example.focusscrambler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar; // Changed import
import android.widget.Switch; // Changed import
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.Locale;

public class account_page extends AppCompatActivity {

    private Toolbar toolbar;
    private CardView profileCard;
    private ImageView profileImage;
    private ImageView cameraIcon;
    private BottomNavigationView navView;

    // Personal Info TextViews
    private TextView tvUsername, tvFirstName, tvLastName, tvEmail;

    // SeekBar and Switch (AppCompat compatible)
    private SeekBar seekbarFocusDuration; // Changed type
    private TextView tvFocusDurationValue;
    private SeekBar seekbarBreakDuration; // Changed type
    private TextView tvBreakDurationValue;
    private Switch switchDoNotDisturb; // Changed type

    // Buttons
    private Button btnSaveChanges;
    private CardView cardLogout;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        // Initialize UI elements
        profileCard = findViewById(R.id.profileCard);
        profileImage = findViewById(R.id.profileImage);
        cameraIcon = findViewById(R.id.cameraIcon);
        navView = findViewById(R.id.nav_view);

        // Personal Info Text Views
        tvUsername = findViewById(R.id.tv_username);
        tvFirstName = findViewById(R.id.tv_first_name);
        tvLastName = findViewById(R.id.tv_last_name);
        tvEmail = findViewById(R.id.tv_email);

        // SeekBar and Switch (AppCompat compatible)
        seekbarFocusDuration = findViewById(R.id.seekbar_focus_duration); // Changed ID
        tvFocusDurationValue = findViewById(R.id.tv_focus_duration_value);
        seekbarBreakDuration = findViewById(R.id.seekbar_break_duration); // Changed ID
        tvBreakDurationValue = findViewById(R.id.tv_break_duration_value);
        switchDoNotDisturb = findViewById(R.id.switch_do_not_disturb); // Changed type

        // Buttons
        btnSaveChanges = findViewById(R.id.btn_save_settings);
        cardLogout = findViewById(R.id.card_logout);


        // --- Set up the BottomNavigationView Listener ---
        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(this, dashboard_withnav_page.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_activities) {
                Toast.makeText(this, "Activities clicked (not implemented)", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.navigation_account) {
                return true;
            }
            return false;
        });

        // --- Programmatically select the 'Account' item when this page loads ---
        navView.setSelectedItemId(R.id.navigation_account);


        // --- Initialize ActivityResultLauncher for image picking ---
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            profileImage.setImageBitmap(bitmap);
                            Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });

        profileCard.setOnClickListener(v -> openImagePicker());
        cameraIcon.setOnClickListener(v -> openImagePicker());

        // --- Set up SeekBar Listeners ---
        seekbarFocusDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // SeekBars don't have stepSize directly like Sliders, so we manually adjust based on min/step
                int min = seekBar.getMin();
                int steppedProgress = ((progress - min) / 5) * 5 + min; // Ensure it snaps to 5s
                if (steppedProgress < min) steppedProgress = min; // ensure it doesn't go below min if progress is very small
                seekBar.setProgress(steppedProgress); // Update seekBar visually if needed

                tvFocusDurationValue.setText(String.format(Locale.getDefault(), "%d min", steppedProgress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekbarFocusDuration.setMin(5); // Set min programmatically as well for older API compatibility
        seekbarFocusDuration.setMax(90); // Max is also for older API

        seekbarBreakDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // For a step size of 1, progress is usually fine.
                // If step size is different, apply similar logic as above.
                tvBreakDurationValue.setText(String.format(Locale.getDefault(), "%d min", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekbarBreakDuration.setMin(1);
        seekbarBreakDuration.setMax(30);

        // Set initial values for TextViews (after setting listeners to avoid issues)
        tvFocusDurationValue.setText(String.format(Locale.getDefault(), "%d min", seekbarFocusDuration.getProgress()));
        tvBreakDurationValue.setText(String.format(Locale.getDefault(), "%d min", seekbarBreakDuration.getProgress()));

        // --- Set up Save and Logout Button Listeners ---
        btnSaveChanges.setOnClickListener(v -> {
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show();
            // Implement save logic here:
            // int focusDuration = seekbarFocusDuration.getProgress();
            // int breakDuration = seekbarBreakDuration.getProgress();
            // boolean dndEnabled = switchDoNotDisturb.isChecked();
        });

        cardLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
            // Implement logout logic here:
            // Intent logoutIntent = new Intent(this, login_page.class);
            // logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(logoutIntent);
            // finish();
        });

        // Optional: Load existing settings when activity starts
        loadUserSettings();
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    private void loadUserSettings() {
        // Example: Load values from SharedPreferences or a database
        // For now, setting some dummy values
        tvUsername.setText("FocusMaster");
        tvFirstName.setText("Marius");
        tvLastName.setText("Banas");
        tvEmail.setText("marius.banas@example.com");

        // Set SeekBar progress for initial loading
        seekbarFocusDuration.setProgress(30);
        tvFocusDurationValue.setText(String.format(Locale.getDefault(), "%d min", 30));

        seekbarBreakDuration.setProgress(10);
        tvBreakDurationValue.setText(String.format(Locale.getDefault(), "%d min", 10));

        switchDoNotDisturb.setChecked(true);
    }
}