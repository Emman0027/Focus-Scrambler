package com.example.focusscrambler;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
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

import android.app.AlertDialog;
import android.widget.EditText;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class account_page extends AppCompatActivity {

    private Toolbar toolbar;
    private CardView profileCard;
    private ImageView profileImage;
    private ImageView cameraIcon;
    private BottomNavigationView navView;

    // Personal Info TextViews
    private TextView tvUsername, tvFirstName, tvLastName, tvEmail;

    // SeekBar and Switch
    private SeekBar seekbarFocusDuration;
    private TextView tvFocusDurationValue;
    private SeekBar seekbarBreakDuration;
    private TextView tvBreakDurationValue;
    private Switch switchDoNotDisturb;

    // Buttons
    private Button btnSaveChanges;
    private CardView cardLogout;

    private ActivityResultLauncher<String> pickImageLauncher;
    private DatabaseManager dbManager;
    private long currentUserId = -1; // Track logged-in user
    private ImageView ivEditUsername, ivEditFirstName, ivEditLastName;
    private boolean isEditingUsername = false, isEditingFirstName = false, isEditingLastName = false;

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

        // ✅ NEW: Initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (Exception e) {
            Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get current user ID from SharedPreferences (set during login)
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
        setupSeekBars();
        setupButtons();

        // ✅ LOAD REAL USER DATA
        loadUserSettings();
    }

    /** ✅ NEW: Get logged-in user ID from SharedPreferences */
    private long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        return prefs.getLong("current_user_id", -1);
    }

    /** ✅ NEW: Load REAL user data from database */
    private void loadUserSettings() {
        Cursor cursor = null;
        try {
            // Fetch user by ID
            cursor = dbManager.fetchUserById(currentUserId);

            if (cursor != null && cursor.moveToFirst()) {
                // ✅ Populate UI with REAL database data
                tvFirstName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_FIRST_NAME)));
                tvLastName.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_LAST_NAME)));
                tvUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_USERNAME)));
                tvEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL)));

                Toast.makeText(this, "Loaded user data", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Load settings (SeekBars, Switch) from SharedPreferences
        loadAppSettings();
    }

    /** ✅ NEW: Load app settings from SharedPreferences */
    private void loadAppSettings() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);

        seekbarFocusDuration.setProgress(prefs.getInt("focus_duration", 30));
        tvFocusDurationValue.setText(String.format(Locale.getDefault(), "%d min", prefs.getInt("focus_duration", 30)));

        seekbarBreakDuration.setProgress(prefs.getInt("break_duration", 10));
        tvBreakDurationValue.setText(String.format(Locale.getDefault(), "%d min", prefs.getInt("break_duration", 10)));

        switchDoNotDisturb.setChecked(prefs.getBoolean("dnd_enabled", true));
    }

    /** Save settings to SharedPreferences */
    private void saveAppSettings() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("focus_duration", seekbarFocusDuration.getProgress());
        editor.putInt("break_duration", seekbarBreakDuration.getProgress());
        editor.putBoolean("dnd_enabled", switchDoNotDisturb.isChecked());
        editor.apply();

        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
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

        seekbarFocusDuration = findViewById(R.id.seekbar_focus_duration);
        tvFocusDurationValue = findViewById(R.id.tv_focus_duration_value);
        seekbarBreakDuration = findViewById(R.id.seekbar_break_duration);
        tvBreakDurationValue = findViewById(R.id.tv_break_duration_value);
        switchDoNotDisturb = findViewById(R.id.switch_do_not_disturb);

        btnSaveChanges = findViewById(R.id.btn_save_settings);
        cardLogout = findViewById(R.id.card_logout);

        // ✅ Find Edit Icons (give them IDs in XML first - see Step 2)
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

    private void setupSeekBars() {
        seekbarFocusDuration.setMin(5);
        seekbarFocusDuration.setMax(90);
        seekbarFocusDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int min = seekBar.getMin();
                int steppedProgress = ((progress - min) / 5) * 5 + min;
                if (steppedProgress < min) steppedProgress = min;
                seekBar.setProgress(steppedProgress);
                tvFocusDurationValue.setText(String.format(Locale.getDefault(), "%d min", steppedProgress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekbarBreakDuration.setMin(1);
        seekbarBreakDuration.setMax(30);
        seekbarBreakDuration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvBreakDurationValue.setText(String.format(Locale.getDefault(), "%d min", progress));
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupButtons() {
        btnSaveChanges.setOnClickListener(v -> saveAppSettings());

        cardLogout.setOnClickListener(v -> {
            // Clear login state
            SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("current_user_id");
            editor.apply();

            Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, login_page.class));
            finish();
        });
    }

    // ✅ NEW: Setup all edit listeners
    private void setupEditListeners() {
        // Username Card + Edit Icon
        findViewById(R.id.card_username).setOnClickListener(v -> showEditDialog("username"));
        ivEditUsername.setOnClickListener(v -> showEditDialog("username"));

        // First Name Card + Edit Icon
        findViewById(R.id.card_first_name).setOnClickListener(v -> showEditDialog("first_name"));
        ivEditFirstName.setOnClickListener(v -> showEditDialog("first_name"));

        // Last Name Card + Edit Icon
        findViewById(R.id.card_last_name).setOnClickListener(v -> showEditDialog("last_name"));
        ivEditLastName.setOnClickListener(v -> showEditDialog("last_name"));
    }

    // ✅ NEW: Universal Edit Dialog
    private void showEditDialog(String fieldType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit " + fieldType.toUpperCase());

        // Create EditText dynamically
        final EditText editText = new EditText(this);
        editText.setPadding(48, 48, 48, 48);

        // Pre-fill current value
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
        editText.setSelection(currentValue.length()); // Focus cursor at end

        builder.setView(editText);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newValue = editText.getText().toString().trim();

            // Validation
            if (newValue.isEmpty()) {
                Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update UI immediately
            updateFieldUI(fieldType, newValue);

            // ✅ Update DATABASE
            updateUserInDatabase(fieldType, newValue);

            hideKeyboard();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // ✅ NEW: Update TextView UI
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

    // ✅ NEW: Update database
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

    // ✅ NEW: Hide keyboard utility
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
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }


}