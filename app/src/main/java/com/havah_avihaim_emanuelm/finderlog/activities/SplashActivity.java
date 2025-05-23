package com.havah_avihaim_emanuelm.finderlog.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.havah_avihaim_emanuelm.finderlog.R;

public class SplashActivity extends AppCompatActivity {

    public static final int APP_START_SEC = 7;
    private int timer;
    private TextView txvAppStart;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Window window = getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);

        txvAppStart = findViewById(R.id.txvAppStartID);
        txvAppStart.setText(getString(R.string.app_will_start, APP_START_SEC));
        new Thread(() ->
        {
            timer = APP_START_SEC;
            while(timer != 0)
            {
                SystemClock.sleep(1000);
                timer--;
                runOnUiThread(() -> txvAppStart.setText(getString(R.string.app_will_start, timer)));
            }
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish(); // don't push this activity to stack
        }).start();
    }
}
