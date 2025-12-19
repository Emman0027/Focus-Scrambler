package com.example.focusscrambler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.Slider; // Import Slider
import com.google.android.material.switchmaterial.SwitchMaterial; // Import SwitchMaterial

import java.io.IOException;
import java.util.Locale; // Import Locale

public class account_page extends AppCompatActivity {

    private Toolbar toolbar;
    private CardView profileCard;
    private ImageView profileImage;
    private ImageView cameraIcon;
    private BottomNavigationView navView;

    // Personal Info TextViews
    private TextView tvUsername, tvFirstName, tvLastName, tvEmail;

    // Sliders and Switch
    private Slider sliderFocusDuration;
    private TextView tvFocusDurationValue;
    private Slider sliderBreakDuration;
    private TextView tvBreakDurationValue;
    private SwitchMaterial switchDoNotDisturb;

    // Buttons
    private Button btnSaveChanges;
    private CardView cardLogout; // Logout is now a CardView

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
        setSupportActionBar(toolbar); // Set the toolbar as the activity's action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true); // Ensure title is shown
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

        // Sliders and Switch
        sliderFocusDuration = findViewById(R.id.slider_focus_duration);
        tvFocusDurationValue = findViewById(R.id.tv_focus_duration_value);
        sliderBreakDuration = findViewById(R.id.slider_break_duration);
        tvBreakDurationValue = findViewById(R.id.tv_break_duration_value);
        switchDoNotDisturb = findViewById(R.id.switch_do_not_disturb);

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
                // Already on this page
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

        // --- Set click listener for the profile picture holder ---
        profileCard.setOnClickListener(v -> openImagePicker());
        cameraIcon.setOnClickListener(v -> openImagePicker());

        // --- Set up Slider Listeners ---
        sliderFocusDuration.addOnChangeListener((slider, value, fromUser) -> {
            tvFocusDurationValue.setText(String.format(Locale.getDefault(), "%.0f min", value));
        });

        sliderBreakDuration.addOnChangeListener((slider, value, fromUser) -> {
            tvBreakDurationValue.setText(String.format(Locale.getDefault(), "%.0f min", value));
        });

        // --- Set up Save and Logout Button Listeners ---
        btnSaveChanges.setOnClickListener(v -> {
            // Implement save logic here
            Toast.makeText(this, "Settings Saved!", Toast.LENGTH_SHORT).show();
            // Example: Save values to SharedPreferences or database
            // float focusDuration = sliderFocusDuration.getValue();
            // float breakDuration = sliderBreakDuration.getValue();
            // boolean dndEnabled = switchDoNotDisturb.isChecked();
        });

        cardLogout.setOnClickListener(v -> {
            // Implement logout logic here
            Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
            // Example: Clear user session, navigate to login page
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

        sliderFocusDuration.setValue(30f); // Set initial slider value
        tvFocusDurationValue.setText(String.format(Locale.getDefault(), "%.0f min", 30f));

        sliderBreakDuration.setValue(10f);
        tvBreakDurationValue.setText(String.format(Locale.getDefault(), "%.0f min", 10f));

        switchDoNotDisturb.setChecked(true); // Example: DND enabled by default
    }
}