package com.example.focusscrambler;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.Slider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class activities_page extends AppCompatActivity implements BreakActivityAdapter.OnItemActionListener {

    private TextView tvSessionDurationTime;
    private Button btnSessionDurationControls;
    private EditText etBreakActivity;
    private Button btnAddBreakActivity;
    private ImageButton btnBreakDurationDropdown;
    private RecyclerView rvBreakActivities, rvDoneActivities;
    private TextView tvPendingHeader, tvDoneHeader;
    private BreakActivityAdapter pendingAdapter, doneAdapter;
    private List<BreakActivity> pendingActivitiesList, doneActivitiesList;
    private BottomNavigationView navView;

    private DatabaseManager dbManager;
    private long currentUserId = -1;

    // Durations
    private int sessionDurationMinutes = 25; // 5-30 minutes range
    private int selectedBreakDurationSeconds = 30; // 0-300 seconds (5 minutes)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_page);

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (Exception e) {
            Log.e("ActivitiesPage", "Error opening database", e);
            Toast.makeText(this, "Database error. Please try again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "Please log in to manage activities.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, login_page.class));
            finish();
            return;
        }

        initViews();
        setupRecyclerViews();
        setupListeners();
        updateSessionDurationDisplay();
    }

    private void initViews() {
        tvSessionDurationTime = findViewById(R.id.tv_session_duration_time);
        btnSessionDurationControls = findViewById(R.id.btn_session_duration_controls);
        etBreakActivity = findViewById(R.id.et_break_activity);
        btnAddBreakActivity = findViewById(R.id.btn_add_break_activity);
        btnBreakDurationDropdown = findViewById(R.id.btn_break_duration_dropdown);
        rvBreakActivities = findViewById(R.id.rv_break_activities);
        rvDoneActivities = findViewById(R.id.rv_done_activities);
        tvPendingHeader = findViewById(R.id.tv_pending_break_activities_header);
        tvDoneHeader = findViewById(R.id.tv_done_activities_header);
        navView = findViewById(R.id.nav_view);
    }

    private void setupRecyclerViews() {
        pendingActivitiesList = new ArrayList<>();
        doneActivitiesList = new ArrayList<>();

        // Adapters are initialized here
        pendingAdapter = new BreakActivityAdapter(pendingActivitiesList, this, false);
        doneAdapter = new BreakActivityAdapter(doneActivitiesList, this, true);

        rvBreakActivities.setLayoutManager(new LinearLayoutManager(this));
        rvBreakActivities.setAdapter(pendingAdapter);

        rvDoneActivities.setLayoutManager(new LinearLayoutManager(this));
        rvDoneActivities.setAdapter(doneAdapter);

        // Load data initially
        loadPendingActivitiesFromDatabase();
        loadDoneActivitiesFromDatabase();
    }

    private void setupListeners() {
        // Session duration adjuster (5-30 minutes)
        btnSessionDurationControls.setOnClickListener(v -> showSessionDurationDialog());

        // Break duration dropdown (0-300 seconds)
        btnBreakDurationDropdown.setOnClickListener(v -> showBreakDurationDialog());

        // Add break activity
        btnAddBreakActivity.setOnClickListener(v -> addBreakActivity());

        // Bottom navigation
        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(this, dashboard_withnav_page.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_activities) {
                return true;
            } else if (item.getItemId() == R.id.navigation_account) {
                startActivity(new Intent(this, account_page.class));
                finish();
                return true;
            }
            return false;
        });
        navView.setSelectedItemId(R.id.navigation_activities);
    }

    private void showSessionDurationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_session_duration);

        Slider sessionSlider = dialog.findViewById(R.id.slider_session_duration);
        TextView tvSessionPreview = dialog.findViewById(R.id.tv_session_preview);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        if (sessionSlider != null) {
            sessionSlider.setValueFrom(5);
            sessionSlider.setValueTo(30);
            sessionSlider.setValue(sessionDurationMinutes);
            tvSessionPreview.setText(sessionDurationMinutes + " minutes"); // Initial value display
            sessionSlider.addOnChangeListener((slider, value, fromUser) -> {
                sessionDurationMinutes = (int) value;
                if (tvSessionPreview != null) {
                    tvSessionPreview.setText(sessionDurationMinutes + " minutes");
                }
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                updateSessionDurationDisplay();
                saveSessionDuration();
                dialog.dismiss();
                Toast.makeText(this, "Session set to " + sessionDurationMinutes + " min", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.90), // 90% of screen width
                WindowManager.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void showBreakDurationDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_break_duration);

        Slider breakSlider = dialog.findViewById(R.id.slider_break_duration);
        TextView tvBreakPreview = dialog.findViewById(R.id.tv_break_preview);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

        if (breakSlider != null) {
            breakSlider.setValueFrom(0);
            breakSlider.setValueTo(300); // 5 minutes
            breakSlider.setValue(selectedBreakDurationSeconds);
            tvBreakPreview.setText(formatBreakTime(selectedBreakDurationSeconds)); // Initial value display
            breakSlider.addOnChangeListener((slider, value, fromUser) -> {
                selectedBreakDurationSeconds = (int) value;
                if (tvBreakPreview != null) {
                    tvBreakPreview.setText(formatBreakTime(selectedBreakDurationSeconds));
                }
            });
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                Toast.makeText(this, "Break duration: " + formatBreakTime(selectedBreakDurationSeconds), Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
        dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.90), // 90% of screen width
                WindowManager.LayoutParams.WRAP_CONTENT
        );
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void addBreakActivity() {
        String activityText = etBreakActivity.getText().toString().trim();
        if (!TextUtils.isEmpty(activityText)) {
            String currentDate = getCurrentDate();
            long newBreakId = dbManager.insertBreakActivity(
                    currentUserId, currentDate, activityText, selectedBreakDurationSeconds);

            if (newBreakId != -1) {
                // Add to the pending list immediately
                pendingActivitiesList.add(new BreakActivity(activityText, selectedBreakDurationSeconds, newBreakId, false));
                pendingAdapter.notifyItemInserted(pendingActivitiesList.size() - 1);
                etBreakActivity.setText("");
                Toast.makeText(this, "Break activity added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add break activity.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please enter a break activity", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSessionDurationDisplay() {
        tvSessionDurationTime.setText(formatTime(sessionDurationMinutes * 60));
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private String formatBreakTime(int seconds) {
        if (seconds >= 60) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return String.format("%d min %02ds", minutes, secs);
        } else {
            return seconds + "s";
        }
    }

    private void saveSessionDuration() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        prefs.edit().putInt("session_duration_minutes", sessionDurationMinutes).apply();
    }

    private long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        return prefs.getLong("current_user_id", -1);
    }

    private void loadPendingActivitiesFromDatabase() {
        Cursor cursor = null;
        pendingActivitiesList.clear(); // Clear existing data
        try {
            cursor = dbManager.getBreakActivitiesForUser(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.BREAK_ID));
                    String task = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BREAK_TASK));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BREAK_DURATION)); // This is in seconds
                    pendingActivitiesList.add(new BreakActivity(task, duration, id, false)); // false because it's pending
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ActivitiesPage", "Error loading pending activities", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        pendingAdapter.notifyDataSetChanged(); // Notify adapter after data changes
    }

    private void loadDoneActivitiesFromDatabase() {
        Cursor cursor = null;
        doneActivitiesList.clear(); // Clear existing data
        try {
            cursor = dbManager.getHistoryEntriesForUser(currentUserId); // Use the new history method
            if (cursor != null && cursor.moveToFirst()) {
                int activityNameIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_ACTIVITY_NAME);
                int durationMinutesIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_DURATION_MINUTES);
                int historyIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_ID);

                do {
                    String activityName = cursor.getString(activityNameIndex);
                    int durationMinutes = cursor.getInt(durationMinutesIndex);
                    long historyId = cursor.getLong(historyIdIndex);
                    // For done activities, the ID here refers to the history_id,
                    // and duration is in minutes from history table, convert to seconds for BreakActivity
                    doneActivitiesList.add(new BreakActivity(activityName, durationMinutes * 60, historyId, true)); // true because it's completed
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("ActivitiesPage", "Error loading done activities from history", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        doneAdapter.notifyDataSetChanged(); // Notify adapter after data changes
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public void onDeleteClick(int position, boolean isDoneList) {
        BreakActivity activityToDelete = null;
        long idToDelete = -1;
        String activityDescription = "";

        if (isDoneList) {
            if (position >= 0 && position < doneActivitiesList.size()) {
                activityToDelete = doneActivitiesList.get(position);
                idToDelete = activityToDelete.getId(); // This is the HISTORY_ID for done list
                activityDescription = activityToDelete.getDescription();

                // Delete from history table
                int rowsAffected = dbManager.deleteHistoryEntry(idToDelete); // New method needed in DatabaseManager
                if (rowsAffected > 0) {
                    doneActivitiesList.remove(position);
                    doneAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Done activity '" + activityDescription + "' deleted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to delete done activity '" + activityDescription + "'.", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (position >= 0 && position < pendingActivitiesList.size()) {
                activityToDelete = pendingActivitiesList.get(position);
                idToDelete = activityToDelete.getId(); // This is the BREAK_ID for pending list
                activityDescription = activityToDelete.getDescription();

                // Delete from break_activities table
                int rowsAffected = dbManager.deleteBreakActivity(idToDelete);
                if (rowsAffected > 0) {
                    pendingActivitiesList.remove(position);
                    pendingAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Pending activity '" + activityDescription + "' deleted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to delete pending activity '" + activityDescription + "'.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reload data to reflect any changes from other activities (e.g., dashboard)
        loadPendingActivitiesFromDatabase();
        loadDoneActivitiesFromDatabase();
        navView.setSelectedItemId(R.id.navigation_activities); // Ensure correct item is selected
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}