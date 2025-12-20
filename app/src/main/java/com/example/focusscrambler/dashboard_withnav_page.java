package com.example.focusscrambler;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.AlertDialog;
import android.view.LayoutInflater;
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

    // 1. Declare UI elements and Timer Variables

    // UI elements for the main Activity (declared in the code below)
    private ProgressBar progressBar;
    private BottomNavigationView navView;
    private Button btnMainPlayTrigger;

    // UI elements that will be located INSIDE the dialog (declared in the code above)
    private TextView txt_timer;
    private Button btn_start, btn_stop, btn_pause, btn_add, btn_subtract;
    private AlertDialog timerDialog;

    // Timer state and data variables (declared in both)
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private long currentSessionDuration;
    private boolean isTimerRunning = false;

    // Constants (Cleaned up and maintained the set used in the previous solution)
    private static final long DEFAULT_DURATION = 25 * 60 * 1000; // 25:00
    private static final long MIN_DURATION = 5 * 60 * 1000;      // 05:00
    private static final long MAX_DURATION = 95 * 60 * 1000;     // 95:00
    private static final long ADJUSTMENT_AMOUNT = 5 * 60 * 1000; // 5 minutes

    // Calendar RecyclerView elements
    private RecyclerView calendarRecyclerView;
    private CalendarDateAdapter calendarAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_withnav_page);

        // 2. Initialize Permanent Views (from Activity layout)
        progressBar = findViewById(R.id.progressBar3);
        navView = findViewById(R.id.nav_view);
        btnMainPlayTrigger = findViewById(R.id.btn_main_play_trigger);

        // Initial setup
        timeLeftInMillis = DEFAULT_DURATION;
        currentSessionDuration = DEFAULT_DURATION; // Initialize current session duration
        updateActivityUI();

        // Set listener for the main play button to open the dialog
        btnMainPlayTrigger.setOnClickListener(v -> showTimerDialog());

        // 3. Setup Navigation Listener
        navView.setOnItemSelectedListener(item -> {
            // Handle navigation clicks here, similar to your dashboard_withnav_page
            // For example:
            if (item.getItemId() == R.id.navigation_home) {
                //startActivity(new Intent(this, dashboard_withnav_page.class));
                //finish(); // Optional: finish current activity
                Toast.makeText(this, "Home already selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.navigation_activities) {
                startActivity(new Intent(this, activities_page.class));
                finish(); // Optional: finish current activity
                Toast.makeText(this, "Activities clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.navigation_account) {
                startActivity(new Intent(this, account_page.class));
                finish(); // Optional: finish current activity
                Toast.makeText(this, "Account clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // 4. Handle Quick Action Clicks
        Button btnAddActivity = findViewById(R.id.btn_add_activity);
        Button btnHistory = findViewById(R.id.btn_history);

        btnAddActivity.setOnClickListener(v -> Toast.makeText(this, "Add Activity Clicked", Toast.LENGTH_SHORT).show());
        btnHistory.setOnClickListener(v -> Toast.makeText(this, "History Clicked", Toast.LENGTH_SHORT).show());

        // --- Initialize and set up the Calendar RecyclerView ---
        setupCalendarRecyclerView();
    }

    // -------------------------------------------------------------------------
    // CALENDAR RECYCLERVIEW SETUP METHODS
    // -------------------------------------------------------------------------

    private void setupCalendarRecyclerView() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);

        // LinearLayoutManager for horizontal scrolling.
        // It's also defined in XML, but can be set here if not.
        // calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<CalendarDate> dates = generateSampleDates();
        calendarAdapter = new CalendarDateAdapter(dates);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private List<CalendarDate> generateSampleDates() {
        List<CalendarDate> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        Calendar today = (Calendar) calendar.clone(); // Get today's date for comparison

        // Start from beginning of the week (e.g., Sunday or Monday)
        // Adjust this if you want the calendar to start with today's date,
        // or a specific past/future day. For now, it starts at the beginning of the week.
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());


        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault()); // e.g., "Mon"

        for (int i = 0; i < 14; i++) { // Generate 14 days (current week + next week)
            String dayOfWeek = dayFormat.format(calendar.getTime());
            int dateNumber = calendar.get(Calendar.DAY_OF_MONTH);

            // Check if this date is today's date
            boolean isCurrentDay = (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH));

            dateList.add(new CalendarDate(dayOfWeek, dateNumber, isCurrentDay));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return dateList;
    }

    // -------------------------------------------------------------------------
    // DIALOG AND TIMER CONTROL METHODS
    // -------------------------------------------------------------------------

    private void showTimerDialog() {
        // Prevent opening dialog if timer is running and the user somehow triggered the button
        if (isTimerRunning) {
            Toast.makeText(this, "Timer is currently running.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_timer_control, null); // Assumes dialog_timer_control.xml exists

        // 2. Initialize UI elements from the dialog view
        txt_timer = dialogView.findViewById(R.id.dialog_txt_timer);
        btn_start = dialogView.findViewById(R.id.dialog_btn_start);
        btn_pause = dialogView.findViewById(R.id.dialog_btn_pause);
        btn_stop = dialogView.findViewById(R.id.dialog_btn_stop);
        btn_add = dialogView.findViewById(R.id.dialog_btn_add);
        btn_subtract = dialogView.findViewById(R.id.dialog_btn_subtract);

        // 3. Update dialog UI state and set up listeners
        updateDialogUI();

        btn_add.setOnClickListener(v -> adjustTimer(ADJUSTMENT_AMOUNT));
        btn_subtract.setOnClickListener(v -> adjustTimer(-ADJUSTMENT_AMOUNT));

        btn_start.setOnClickListener(v -> startTimer(timeLeftInMillis));

        btn_pause.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else if (timeLeftInMillis > 1000) {
                resumeTimer();
            }
        });

        btn_stop.setOnClickListener(v -> stopTimer());

        // 4. Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        timerDialog = builder.create();
        timerDialog.show();
    }

    // -------------------------------------------------------------------------
    // TIMER LOGIC METHODS
    // -------------------------------------------------------------------------

    private void startTimer(long duration) {
        if (isTimerRunning) return;

        currentSessionDuration = duration; // Save the starting duration for progress bar
        timeLeftInMillis = duration;
        isTimerRunning = true;

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateDialogUI();
                updateActivityUI(); // Update the progress bar on the main activity
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                Toast.makeText(dashboard_withnav_page.this, "Session Finished!", Toast.LENGTH_SHORT).show();
                stopTimer();
            }
        }.start();

        // Update UI immediately after starting
        updateDialogUI();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        updateDialogUI();
    }

    private void resumeTimer() {
        if (timeLeftInMillis > 1000) {
            startTimer(timeLeftInMillis);
        }
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timeLeftInMillis = DEFAULT_DURATION; // Reset to the default time
        currentSessionDuration = DEFAULT_DURATION; // Reset current session duration

        // Dismiss the dialog if it's open
        if (timerDialog != null && timerDialog.isShowing()) {
            timerDialog.dismiss();
        }

        // Reset the state of the main Activity UI (progress bar)
        updateActivityUI();
    }

    // -------------------------------------------------------------------------
    // TIME ADJUSTMENT METHOD
    // -------------------------------------------------------------------------

    private void adjustTimer(long adjustment) {
        if (isTimerRunning) {
            Toast.makeText(this, "Pause timer to adjust time.", Toast.LENGTH_SHORT).show();
            return;
        }

        long newTime = timeLeftInMillis + adjustment;

        if (newTime < MIN_DURATION) {
            timeLeftInMillis = MIN_DURATION;
            Toast.makeText(this, "Minimum duration is 05:00 minutes.", Toast.LENGTH_SHORT).show();
        }
        else if (newTime > MAX_DURATION) {
            timeLeftInMillis = MAX_DURATION;
            Toast.makeText(this, "Maximum duration is 95:00 minutes.", Toast.LENGTH_SHORT).show();
        }
        else {
            timeLeftInMillis = newTime;
        }

        // Update currentSessionDuration to match the adjusted time
        currentSessionDuration = timeLeftInMillis;

        // Update the dialog UI to reflect the new time
        updateDialogUI();
    }

    // -------------------------------------------------------------------------
    // UI UPDATE METHODS
    // -------------------------------------------------------------------------

    private void updateDialogUI() {
        if (txt_timer == null) return; // Safety check if dialog is not visible

        // 1. Update Time Display
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);

        txt_timer.setText(timeFormatted);

        // 2. Update Button States

        // Adjustment buttons enabled only when timer is NOT running
        boolean enableAdjustment = !isTimerRunning;
        btn_add.setEnabled(enableAdjustment);
        btn_subtract.setEnabled(enableAdjustment);

        btn_stop.setEnabled(isTimerRunning || timeLeftInMillis < currentSessionDuration);

        if (isTimerRunning) {
            // State: RUNNING
            btn_start.setEnabled(false);
            btn_pause.setEnabled(true);
            btn_pause.setText("Pause");
        } else if (timeLeftInMillis == currentSessionDuration) {
            // State: STOPPED/Initial Duration Set
            btn_start.setEnabled(true);
            btn_start.setText("Start");
            btn_pause.setEnabled(false);
            btn_pause.setText("Pause");
        } else {
            // State: PAUSED or Adjusted time set but not started
            btn_start.setEnabled(true);
            btn_start.setText("Resume");
            btn_pause.setEnabled(false);
            btn_pause.setText("Pause");
        }
    }

    // Method to update the ProgressBar on the main Activity screen
    private void updateActivityUI() {
        if (progressBar == null) return; // Safety check for main Activity view

        if (isTimerRunning) {
            // Update progress bar based on the current session duration
            int progress = (int) (((double) (currentSessionDuration - timeLeftInMillis) / currentSessionDuration) * 100);
            progressBar.setProgress(progress);
        } else {
            // When timer is stopped, reset progress to 0
            progressBar.setProgress(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the timer when the Activity is destroyed
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}