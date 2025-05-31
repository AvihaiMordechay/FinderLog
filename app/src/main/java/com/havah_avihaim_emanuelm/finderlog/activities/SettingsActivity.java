package com.havah_avihaim_emanuelm.finderlog.activities;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.havah_avihaim_emanuelm.finderlog.R;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchNotifications;
    private SharedPreferences prefs;

    private static final int REQUEST_NOTIFICATION_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNotifications = findViewById(R.id.switch_notifications);
        Spinner spinnerTimeRange = findViewById(R.id.spinner_time_range);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Load saved state
        boolean isEnabled = prefs.getBoolean("notifications_enabled", false);
        switchNotifications.setChecked(isEnabled);


        // Notifications switch logic
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();

            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Check and request notification permission only on Android 13+
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_NOTIFICATION_PERMISSION);
                }
            }

            Toast.makeText(this,
                    "Notifications " + (isChecked ? "enabled" : "disabled"),
                    Toast.LENGTH_SHORT).show();
        });

        // Set up Spinner with options
        String[] options = {"Last month", "Last 3 months", "Last year"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(adapter);

        // Restore previous selection
        int savedRange = prefs.getInt("selected_range", 0);
        spinnerTimeRange.setSelection(savedRange);

        // Save selected range
        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                prefs.edit().putInt("selected_range", position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    // Optional: Handle user response to permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                // Optionally, turn off the switch if denied
                switchNotifications.setChecked(false);
                prefs.edit().putBoolean("notifications_enabled", false).apply();
            }
        }
    }
}