package com.example.focusscrambler;

import android.Manifest;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

import android.app.AlertDialog;
import android.widget.EditText;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class account_page extends AppCompatActivity {

    private static final int DND_PERMISSION_REQUEST_CODE = 1001;

    private Toolbar toolbar;
    private CardView profileCard;
    private ImageView profileImage;
    private ImageView cameraIcon;
    private BottomNavigationView navView;

    // Personal Info TextViews
    private TextView tvUsername, tvFirstName, tvLastName, tvEmail;

    // Do Not Disturb Switch
    private Switch switchDoNotDisturb;

    // Logout Card
    private CardView cardLogout;

    private ActivityResultLauncher<String> pickImageLauncher;
    private DatabaseManager dbManager;
    private long currentUserId = -1;
    private ImageView ivEditUsername, ivEditFirstName, ivEditLastName;

    // DND Components
    private NotificationManager notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Initialize NotificationManager for DND
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (Exception e) {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get current user ID
        currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, login_page.class));
            finish();
            return;
        }

        initUI();
        setupNavigation();
        setupImagePicker();
        setupDoNotDisturb();
        setupButtons();

        // Load user data
        loadUserSettings();
    }

    private long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        return prefs.getLong("current_user_id", -1);
    }

    private void loadUserSettings() {
        Cursor cursor = null;
        try {
            cursor = dbManager.fetchUserById(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                tvFirstName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_FIRST_NAME)));
                tvLastName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_LAST_NAME)));
                tvUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_USERNAME)));
                tvEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL)));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Load DND setting and sync with system
        loadDoNotDisturbSetting();
    }

    private void loadDoNotDisturbSetting() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        boolean isDndEnabled = prefs.getBoolean("dnd_enabled", false);
        switchDoNotDisturb.setChecked(isDndEnabled);

        // Sync switch state with actual system DND state
        syncDndSwitchWithSystem();
    }

    /**
     * ✅ FULL DND IMPLEMENTATION with Android System Integration
     */
    private void setupDoNotDisturb() {
        switchDoNotDisturb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableDoNotDisturb();
            } else {
                disableDoNotDisturb();
            }
        });
    }

    private void enableDoNotDisturb() {
        // Check permissions first
        if (!hasDndPermission()) {
            requestDndPermission();
            return;
        }

        try {
            SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dnd_enabled", true);
            editor.apply();

            // Set system DND mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
                Toast.makeText(this, "✅ Do Not Disturb enabled (Priority only)", Toast.LENGTH_SHORT).show();
            } else {
                // Fallback for older Android versions
                Toast.makeText(this, "✅ Do Not Disturb enabled", Toast.LENGTH_SHORT).show();
            }

            Log.d("DND", "Do Not Disturb enabled by Focus Scrambler");
        } catch (SecurityException e) {
            Toast.makeText(this, "❌ DND permission denied. Please enable manually.", Toast.LENGTH_LONG).show();
            switchDoNotDisturb.setChecked(false);
        }
    }

    private void disableDoNotDisturb() {
        try {
            SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dnd_enabled", false);
            editor.apply();

            // Turn off system DND
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                Toast.makeText(this, "✅ Do Not Disturb disabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "✅ Do Not Disturb disabled", Toast.LENGTH_SHORT).show();
            }

            Log.d("DND", "Do Not Disturb disabled by Focus Scrambler");
        } catch (SecurityException e) {
            Toast.makeText(this, "❌ Cannot disable DND. Permission required.", Toast.LENGTH_SHORT).show();
            switchDoNotDisturb.setChecked(true);
        }
    }

    /**
     * Check if app has DND permission
     */
    private boolean hasDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return notificationManager.isNotificationPolicyAccessGranted();
        }
        return true; // Older versions don't need permission
    }

    /**
     * Request DND permission from user
     */
    private void requestDndPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                new AlertDialog.Builder(this)
                        .setTitle("Enable Do Not Disturb")
                        .setMessage("Focus Scrambler needs permission to control Do Not Disturb mode. Allow access?")
                        .setPositiveButton("Settings", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            switchDoNotDisturb.setChecked(false);
                        })
                        .show();
            }
        }
    }

    /**
     * Sync switch with actual system DND state
     */
    private void syncDndSwitchWithSystem() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int currentInterruptionFilter = notificationManager.getCurrentInterruptionFilter();
            boolean systemDndActive = currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL;

            SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
            boolean appDndState = prefs.getBoolean("dnd_enabled", false);

            // Sync if there's a mismatch
            if (systemDndActive != appDndState) {
                switchDoNotDisturb.setChecked(systemDndActive);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("dnd_enabled", systemDndActive);
                editor.apply();
            }
        }
    }

    private void initUI() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        profileCard = findViewById(R.id.profileCard);
        profileImage = findViewById(R.id.profileImage);
        cameraIcon = findViewById(R.id.cameraIcon);
        navView = findViewById(R.id.nav_view);

        tvUsername = findViewById(R.id.tv_username);
        tvFirstName = findViewById(R.id.tv_first_name);
        tvLastName = findViewById(R.id.tv_last_name);
        tvEmail = findViewById(R.id.tv_email);

        switchDoNotDisturb = findViewById(R.id.switch_do_not_disturb);
        cardLogout = findViewById(R.id.card_logout);

        ivEditUsername = findViewById(R.id.iv_edit_username);
        ivEditFirstName = findViewById(R.id.iv_edit_first_name);
        ivEditLastName = findViewById(R.id.iv_edit_last_name);

        setupEditListeners();
    }

    private void setupNavigation() {
        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(this, dashboard_withnav_page.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_activities) {
                startActivity(new Intent(this, activities_page.class));
            }
            return item.getItemId() == R.id.navigation_account;
        });
        navView.setSelectedItemId(R.id.navigation_account);
    }

    private void setupImagePicker() {
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
                    }
                });

        profileCard.setOnClickListener(v -> openImagePicker());
        cameraIcon.setOnClickListener(v -> openImagePicker());
    }

    private void setupButtons() {
        cardLogout.setOnClickListener(v -> {
            // Disable DND before logout
            disableDoNotDisturb();

            SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("current_user_id");
            editor.apply();

            Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, login_page.class));
            finish();
        });
    }

    private void setupEditListeners() {
        findViewById(R.id.card_username).setOnClickListener(v -> showEditDialog("username"));
        ivEditUsername.setOnClickListener(v -> showEditDialog("username"));

        findViewById(R.id.card_first_name).setOnClickListener(v -> showEditDialog("first_name"));
        ivEditFirstName.setOnClickListener(v -> showEditDialog("first_name"));

        findViewById(R.id.card_last_name).setOnClickListener(v -> showEditDialog("last_name"));
        ivEditLastName.setOnClickListener(v -> showEditDialog("last_name"));
    }

    private void showEditDialog(String fieldType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + fieldType.toUpperCase());

        final EditText editText = new EditText(this);
        editText.setPadding(48, 48, 48, 48);

        String currentValue = "";
        switch (fieldType) {
            case "username":
                currentValue = tvUsername.getText().toString();
                break;
            case "first_name":
                currentValue = tvFirstName.getText().toString();
                break;
            case "last_name":
                currentValue = tvLastName.getText().toString();
                break;
        }
        editText.setText(currentValue);
        editText.setSelection(currentValue.length());

        builder.setView(editText);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newValue = editText.getText().toString().trim();
            if (newValue.isEmpty()) {
                Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            updateFieldUI(fieldType, newValue);
            updateUserInDatabase(fieldType, newValue);
            hideKeyboard();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateFieldUI(String fieldType, String newValue) {
        switch (fieldType) {
            case "username":
                tvUsername.setText(newValue);
                break;
            case "first_name":
                tvFirstName.setText(newValue);
                break;
            case "last_name":
                tvLastName.setText(newValue);
                break;
        }
        Toast.makeText(this, fieldType.toUpperCase() + " updated!", Toast.LENGTH_SHORT).show();
    }

    private void updateUserInDatabase(String fieldType, String newValue) {
        try {
            ContentValues values = new ContentValues();
            switch (fieldType) {
                case "username":
                    values.put(DatabaseHelper.USER_USERNAME, newValue);
                    break;
                case "first_name":
                    values.put(DatabaseHelper.USER_FIRST_NAME, newValue);
                    break;
                case "last_name":
                    values.put(DatabaseHelper.USER_LAST_NAME, newValue);
                    break;
            }

            String whereClause = DatabaseHelper.USER_ID + " = ?";
            String[] whereArgs = {String.valueOf(currentUserId)};

            int rowsUpdated = dbManager.database.update(
                    DatabaseHelper.TABLE_USERS,
                    values,
                    whereClause,
                    whereArgs
            );

            if (rowsUpdated > 0) {
                Log.d("AccountPage", fieldType + " updated in DB for user " + currentUserId);
            }
        } catch (Exception e) {
            Log.e("AccountPage", "Database update failed", e);
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sync DND state when returning to activity
        syncDndSwitchWithSystem();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}