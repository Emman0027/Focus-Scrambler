package com.example.focusscrambler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class history_page extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter historyAdapter;
    private List<HistoryItem> historyItems = new ArrayList<>();
    private DatabaseManager dbManager;
    private long currentUserId = -1;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupToolbar();
        setupDatabase();
        setupBottomNavigation();
        initRecyclerView();
        loadHistoryData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_history);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupDatabase() {
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (Exception e) {
            Log.e("HistoryActivity", "Database open failed", e);
            Toast.makeText(this, "Database error", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, login_page.class));
            finish();
            return;
        }
    }

    private void setupBottomNavigation() {
        navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                startActivity(new Intent(this, dashboard_withnav_page.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_activities) {
                startActivity(new Intent(this, activities_page.class));
                finish();
                return true;
            } else if (item.getItemId() == R.id.navigation_account) {
                startActivity(new Intent(this, account_page.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        return prefs.getLong("current_user_id", -1);
    }

    private void initRecyclerView() {
        rvHistory = findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(historyItems);
        rvHistory.setAdapter(historyAdapter);
    }

    /** ✅ USES YOUR EXISTING HISTORY TABLE! */
    private void loadHistoryData() {
        historyItems.clear();

        Cursor cursor = null;
        try {
            cursor = dbManager.getHistoryEntriesForUser(currentUserId);

            if (cursor != null && cursor.moveToFirst()) {
                String currentDate = "";

                do {
                    String completionDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_COMPLETION_DATE));
                    String activityName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_ACTIVITY_NAME));
                    int durationMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_DURATION_MINUTES));
                    String completionTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.HISTORY_COMPLETION_TIME));

                    // Add date header if date changed
                    if (!completionDate.equals(currentDate)) {
                        historyItems.add(new HistoryItem.DateHeader(formatDateHeader(completionDate)));
                        currentDate = completionDate;
                    }

                    // Add activity entry
                    long timestamp = System.currentTimeMillis(); // You can store actual timestamp if needed
                    historyItems.add(new HistoryItem.ActivityEntry(activityName, durationMinutes * 60 * 1000L, false, timestamp));

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("HistoryActivity", "Error loading history", e);
            Toast.makeText(this, "No history data yet", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }

        historyAdapter.notifyDataSetChanged();
    }

    /** ✅ Format dates like "Yesterday", "Today", "Dec 17, 2025" */
    private String formatDateHeader(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(dateString);

            Calendar cal = Calendar.getInstance();
            Calendar inputCal = Calendar.getInstance();
            inputCal.setTime(date);

            // Check if today
            if (cal.get(Calendar.YEAR) == inputCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == inputCal.get(Calendar.DAY_OF_YEAR)) {
                return "Today";
            }

            // Check if yesterday
            cal.add(Calendar.DAY_OF_YEAR, -1);
            if (cal.get(Calendar.YEAR) == inputCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == inputCal.get(Calendar.DAY_OF_YEAR)) {
                return "Yesterday";
            }

            // Format as "Dec 17, 2025"
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return outputFormat.format(date);

        } catch (Exception e) {
            return dateString; // Fallback
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistoryData(); // Refresh when returning to screen
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbManager != null) {
            dbManager.close();
        }
    }
}