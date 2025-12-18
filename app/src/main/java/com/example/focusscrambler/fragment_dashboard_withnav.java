package com.example.focusscrambler;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// The class name should follow Java conventions (CamelCase)
// It's best to rename the file to DashboardWithnavFragment.java in the future,
// but for now, this will work.
public class fragment_dashboard_withnav extends Fragment {

    // 1. Declare all your UI elements and timer variables
    private TextView txt_timer;
    private Button btn_start, btn_stop, btn_pause;
    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean isTimerRunning;

    public fragment_dashboard_withnav() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the correct layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard_withnav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Initialize all your views using the fragment's view
        txt_timer = view.findViewById(R.id.txt_timer);
        btn_start = view.findViewById(R.id.btn_start);
        btn_stop = view.findViewById(R.id.btn_stop);
        btn_pause = view.findViewById(R.id.btn_pause);
        progressBar = view.findViewById(R.id.progressBar3); // Use the correct ID from your XML

        // 3. Set up the click listeners for your buttons
        btn_start.setOnClickListener(v -> startTimer(25 * 60 * 1000)); // Example: 25 minutes

        btn_pause.setOnClickListener(v -> {
            if (isTimerRunning) {
                pauseTimer();
            } else {
                resumeTimer();
            }
        });

        btn_stop.setOnClickListener(v -> stopTimer());
    }

    private void startTimer(long duration) {
        timeLeftInMillis = duration;
        isTimerRunning = true;
        btn_pause.setText("Pause");

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerUI();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                Toast.makeText(getContext(), "Session Finished!", Toast.LENGTH_SHORT).show();
                stopTimer(); // Reset UI
            }
        }.start();
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
            btn_pause.setText("Resume");
        }
    }

    private void resumeTimer() {
        startTimer(timeLeftInMillis); // Restart the timer with the remaining time
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        timeLeftInMillis = 0;
        updateTimerUI();
        btn_pause.setText("Pause");
        progressBar.setProgress(100);
    }

    private void updateTimerUI() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        txt_timer.setText(timeFormatted);

        // Update progress bar (e.g., for a 25-minute timer)
        long totalDuration = 25 * 60 * 1000;
        int progress = (int) (((double) (totalDuration - timeLeftInMillis) / totalDuration) * 100);
        progressBar.setProgress(progress);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop the timer when the view is destroyed to prevent memory leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
