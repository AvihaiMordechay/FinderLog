package com.havah_avihaim_emanuelm.finderlog.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.havah_avihaim_emanuelm.finderlog.R;

public class AppSplashActivity extends AppCompatActivity {

    public static final int APP_START_SEC = 7;
    private int timer;
    private TextView txvAppStart;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_splash);

        txvAppStart = findViewById(R.id.txvAppStartID);
        txvAppStart.setText(String.format("app will start in %d sec", APP_START_SEC));
        new Thread(() ->
        {
            timer = APP_START_SEC;
            while(timer != 0)
            {
                SystemClock.sleep(1000);
                timer--;
                runOnUiThread(() -> txvAppStart.setText(String.format("app will start in %d sec", timer)));
            }
            startActivity(new Intent(AppSplashActivity.this, BaseActivity.class));
            finish(); // dont push this activity to stack
        }).start();
    }
}
