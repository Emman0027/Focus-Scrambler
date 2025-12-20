package com.example.focusscrambler;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils; // Import TextUtils
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class activities_page extends AppCompatActivity implements BreakActivityAdapter.OnItemActionListener {

    private TextView tvSessionDurationTime;
    private Button btnSessionDurationControls;
    private EditText etMainActivity;
    private Button btnAddMainActivity;
    private EditText etBreakActivity;
    private Button btnAddBreakActivity;
    private RecyclerView rvBreakActivities;
    private BreakActivityAdapter breakActivityAdapter;
    private List<BreakActivity> breakActivitiesList;
    private BottomNavigationView navView; // Assuming you have bottom nav

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activities_page); // Ensure this matches your XML file name

        // Initialize Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide default title
        }


        // Initialize UI components
        tvSessionDurationTime = findViewById(R.id.tv_session_duration_time);
        btnSessionDurationControls = findViewById(R.id.btn_session_duration_controls);
        etMainActivity = findViewById(R.id.et_main_activity);
        btnAddMainActivity = findViewById(R.id.btn_add_main_activity);
        etBreakActivity = findViewById(R.id.et_break_activity);
        btnAddBreakActivity = findViewById(R.id.btn_add_break_activity);
        rvBreakActivities = findViewById(R.id.rv_break_activities);
        navView = findViewById(R.id.nav_view); // Initialize bottom nav

        // Set up Break Activities RecyclerView
        breakActivitiesList = new ArrayList<>();
        breakActivitiesList.add(new BreakActivity("Go outside and take 10 deep breaths"));
        breakActivitiesList.add(new BreakActivity("Stretch your entire body for 5 minutes, slowly."));
        breakActivitiesList.add(new BreakActivity("Drink Water"));
        breakActivitiesList.add(new BreakActivity("Do 10 pushups"));


        breakActivityAdapter = new BreakActivityAdapter(breakActivitiesList, this); // 'this' because Activity implements OnItemActionListener
        rvBreakActivities.setLayoutManager(new LinearLayoutManager(this));
        rvBreakActivities.setAdapter(breakActivityAdapter);

        // Set Listeners
        btnSessionDurationControls.setOnClickListener(v -> {
            // Implement logic to adjust session duration (e.g., show a picker dialog)
            Toast.makeText(this, "Adjust Session Duration", Toast.LENGTH_SHORT).show();
        });

        btnAddMainActivity.setOnClickListener(v -> {
            String activityText = etMainActivity.getText().toString().trim();
            if (!TextUtils.isEmpty(activityText)) {
                // Implement logic to add main activity
                Toast.makeText(this, "Added Main Activity: " + activityText, Toast.LENGTH_SHORT).show();
                etMainActivity.setText(""); // Clear input
            } else {
                Toast.makeText(this, "Please enter a main activity", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddBreakActivity.setOnClickListener(v -> {
            String activityText = etBreakActivity.getText().toString().trim();
            if (!TextUtils.isEmpty(activityText)) {
                breakActivitiesList.add(new BreakActivity(activityText));
                breakActivityAdapter.notifyItemInserted(breakActivitiesList.size() - 1);
                etBreakActivity.setText(""); // Clear input
            } else {
                Toast.makeText(this, "Please enter a break activity", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up Bottom Navigation Listener (if you have one)
        // This is a placeholder; link it to your actual navigation logic
        navView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                 startActivity(new Intent(this, dashboard_withnav_page.class));
                 finish();
                Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.navigation_activities) {
                // Already on Activities page
                return true;
            } else if (item.getItemId() == R.id.navigation_account) {
                 startActivity(new Intent(this, account_page.class));
                 finish();
                Toast.makeText(this, "Account selected", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        navView.setSelectedItemId(R.id.navigation_activities); // Highlight Activities tab
    }

    @Override
    public void onDeleteClick(int position) {
        breakActivitiesList.remove(position);
        breakActivityAdapter.notifyItemRemoved(position);
        Toast.makeText(this, "Activity deleted", Toast.LENGTH_SHORT).show();
    }
}