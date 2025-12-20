package com.example.focusscrambler;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class dashboard_withnav_page extends AppCompatActivity {

    // UI elements
    private ProgressBar progressBar;
    private BottomNavigationView navView;
    private TextView txtTimerCurrentValue;
    private TextView txtTimerTypeLabel;
    private Button btnStart, btnPause, btnStop;
    private TextView txtSessionsProgress;
    private TextView txtNextSessionActivity;

    // Timer state and data variables
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private long currentSessionTargetDuration;
    private boolean isTimerRunning = false;
    private boolean isFocusSession = true;
    private int currentSessionNumber = 0;
    private int totalSessionsGoal = 3;

    // Database and User specific
    private DatabaseManager dbManager;
    private long currentUserId = -1;
    private List<BreakActivity> availableBreakActivities;
    private int currentBreakActivityIndex = 0;

    // Constants
    private static final long DEFAULT_FOCUS_DURATION = 20 * 1000;
    private static final long DEFAULT_BREAK_DURATION = 20 * 1000;

    // Calendar RecyclerView elements
    private RecyclerView calendarRecyclerView;
    private CalendarDateAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_withnav_page);

        // Initialize DatabaseManager
        dbManager = new DatabaseManager(this);
        try {
            dbManager.open();
        } catch (Exception e) {
            Log.e("Dashboard", "Database open failed", e);
            Toast.makeText(this, "Database error. Please restart app.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get current user ID
        currentUserId = getCurrentUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, login_page.class));
            finish();
            return;
        }

        // Initialize UI elements
        progressBar = findViewById(R.id.progressBar3);
        navView = findViewById(R.id.nav_view);
        txtTimerCurrentValue = findViewById(R.id.txt_timer_current_value);
        txtTimerTypeLabel = findViewById(R.id.txt_timer_type_label);
        btnStart = findViewById(R.id.btn_start);
        btnPause = findViewById(R.id.btn_pause);
        btnStop = findViewById(R.id.btn_stop);
        txtSessionsProgress = findViewById(R.id.txt_sessions);
        txtNextSessionActivity = findViewById(R.id.txt_next_session_activity);

        // Initial setup
        timeLeftInMillis = DEFAULT_FOCUS_DURATION;
        currentSessionTargetDuration = DEFAULT_FOCUS_DURATION;
        isFocusSession = true;
        updateTimerUI();
        updateProgressBar();
        loadBreakActivities();
        updateNextSessionText();
        updateTimerButtonState(); // ✅ Added initial button state

        // Set listeners for timer buttons
        btnStart.setOnClickListener(v -> startTimer());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnStop.setOnClickListener(v -> stopTimer());

        // Setup Navigation Listener
        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                return true;
            } else if (item.getItemId() == R.id.navigation_activities) {
                startActivity(new Intent(this, activities_page.class));
                return true;
            } else if (item.getItemId() == R.id.navigation_account) {
                startActivity(new Intent(this, account_page.class));
                return true;
            }
            return false;
        });

        // Handle Quick Action Clicks
        Button btnAddActivity = findViewById(R.id.btn_add_activity);
        Button btnHistory = findViewById(R.id.btn_history);

        btnAddActivity.setOnClickListener(v -> {
            startActivity(new Intent(this, activities_page.class));
            // finish(); // Keep dashboard open
        });

// ✅ HISTORY BUTTON - Opens history_page
        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, history_page.class));
            // finish(); // Optional: uncomment to close dashboard when going to history
        });

        // Initialize and set up the Calendar RecyclerView
        setupCalendarRecyclerView();
    }

    // --- User Management ---
    private long getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("FocusScramblerPrefs", MODE_PRIVATE);
        return prefs.getLong("current_user_id", -1);
    }

    // --- Break Activities Logic ---
    private void loadBreakActivities() {
        availableBreakActivities = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = dbManager.getBreakActivitiesForUser(currentUserId);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String task = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.BREAK_TASK));
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.BREAK_DURATION));
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.BREAK_ID));
                    availableBreakActivities.add(new BreakActivity(task, duration, id));
                } while (cursor.moveToNext());
            } else {
                availableBreakActivities.add(new BreakActivity("Take a 5-minute walk", 5, -1));
                availableBreakActivities.add(new BreakActivity("Stretch for 5 minutes", 5, -1));
                availableBreakActivities.add(new BreakActivity("Drink a glass of water", 5, -1));
            }
        } catch (Exception e) {
            Log.e("Dashboard", "Error loading break activities", e);
            availableBreakActivities.add(new BreakActivity("Take a short break", 5, -1));
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private BreakActivity getNextBreakActivity() {
        if (availableBreakActivities.isEmpty()) {
            return new BreakActivity("Rest your eyes", 5, -1);
        }
        BreakActivity next = availableBreakActivities.get(currentBreakActivityIndex % availableBreakActivities.size());
        return next;
    }

    private void updateNextSessionText() {
        if (isFocusSession) {
            BreakActivity nextBreak = getNextBreakActivity();
            txtNextSessionActivity.setText(String.format("Break Time (%s)", nextBreak.getDescription()));
        } else {
            txtNextSessionActivity.setText("Focus Time (25 min)");
        }
    }

    // --- Timer Control ---
    private void startTimer() {
        if (isTimerRunning) return;

        isTimerRunning = true;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerUI();
                updateProgressBar();
                updateTimerButtonState(); // ✅ Update buttons on every tick
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                if (isFocusSession) {
                    Toast.makeText(dashboard_withnav_page.this, "Focus Session Finished! Time for a Break!", Toast.LENGTH_SHORT).show();
                    currentSessionNumber++;
                    switchSessionType(false);
                } else {
                    Toast.makeText(dashboard_withnav_page.this, "Break Finished! Back to Focus!", Toast.LENGTH_SHORT).show();
                    currentBreakActivityIndex++;
                    switchSessionType(true);
                }
            }
        }.start();

        updateTimerButtonState(); // ✅ Update after starting
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        updateTimerButtonState(); // ✅ Update after pausing
        Toast.makeText(this, "Timer Paused", Toast.LENGTH_SHORT).show();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        isFocusSession = true;
        timeLeftInMillis = DEFAULT_FOCUS_DURATION;
        currentSessionTargetDuration = DEFAULT_FOCUS_DURATION;
        currentSessionNumber = 0;
        currentBreakActivityIndex = 0;
        updateTimerUI();
        updateProgressBar();
        updateNextSessionText();
        updateTimerButtonState(); // ✅ Update after reset
        Toast.makeText(this, "Timer Stopped and Reset", Toast.LENGTH_SHORT).show();
    }

    private void switchSessionType(boolean toFocus) {
        isFocusSession = toFocus;
        if (isFocusSession) {
            timeLeftInMillis = DEFAULT_FOCUS_DURATION;
            currentSessionTargetDuration = DEFAULT_FOCUS_DURATION;
            txtTimerTypeLabel.setText("Focus Time");
            updateNextSessionText();
        } else {
            BreakActivity breakActivity = getNextBreakActivity();
            timeLeftInMillis = breakActivity.getDurationMinutes() * 60 * 1000L;
            currentSessionTargetDuration = timeLeftInMillis;
            txtTimerTypeLabel.setText("Break Time");
            txtNextSessionActivity.setText("Focus Time (25 min)");
        }
        updateTimerUI();
        updateProgressBar();
        updateTimerButtonState(); // ✅ Update button states
        startTimer();
    }

    // --- UI Update Methods ---
    private void updateTimerUI() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        txtTimerCurrentValue.setText(timeFormatted);
    }

    private void updateProgressBar() {
        if (currentSessionTargetDuration > 0) {
            int progress = (int) (((double) (currentSessionTargetDuration - timeLeftInMillis) / currentSessionTargetDuration) * 100);
            progressBar.setProgress(progress);
        } else {
            progressBar.setProgress(0);
        }
        txtSessionsProgress.setText(String.format(Locale.getDefault(), "%d/%d Sessions", currentSessionNumber, totalSessionsGoal));
    }

    /** ✅ NEW BUTTON STATE LOGIC - Clean 3-state management */
    private void updateTimerButtonState() {
        if (isTimerRunning) {
            // State: RUNNING
            btnStart.setEnabled(false);
            btnPause.setEnabled(true);
            btnPause.setText("Pause");
            btnStop.setEnabled(true);
        } else if (timeLeftInMillis == currentSessionTargetDuration) {
            // State: STOPPED/Initial Duration Set
            btnStart.setEnabled(true);
            btnStart.setText("Start");
            btnPause.setEnabled(false);
            btnPause.setText("Pause");
            btnStop.setEnabled(true);
        } else {
            // State: PAUSED or Adjusted time set but not started
            btnStart.setEnabled(true);
            btnStart.setText("Resume");
            btnPause.setEnabled(false);
            btnPause.setText("Pause");
            btnStop.setEnabled(true);
        }
    }

    // --- Calendar RecyclerView Setup ---
    private void setupCalendarRecyclerView() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        List<CalendarDate> dates = generateSampleDates();
        calendarAdapter = new CalendarDateAdapter(dates);
        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private List<CalendarDate> generateSampleDates() {
        List<CalendarDate> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Calendar today = (Calendar) calendar.clone();

        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (int i = 0; i < 14; i++) {
            String dayOfWeek = dayFormat.format(calendar.getTime());
            int dateNumber = calendar.get(Calendar.DAY_OF_MONTH);
            boolean isCurrentDay = (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));

            dateList.add(new CalendarDate(dayOfWeek, dateNumber, isCurrentDay));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dateList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBreakActivities();
        updateNextSessionText();
        updateTimerButtonState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (dbManager != null) {
            dbManager.close();
        }
    }
}