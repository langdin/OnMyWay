package com.example.onmyway;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(7000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Intent welcome = new Intent(SplashActivity.this, WelcomeActivity.class);
                    startActivity(welcome);
                }
            }
        };

        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        finish();
    }
}
