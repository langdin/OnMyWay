package com.example.onmyway;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    public void goToCustomerEntry(View view) {
        goToActivity(true);
    }

    public void goToDriverEntry(View view) {
        goToActivity(false);
    }

    public void goToActivity(boolean act) {
        Intent i;
        if(act) {
            i = new Intent(WelcomeActivity.this, CustomerEntryActivity.class);
        } else {
            i = new Intent(WelcomeActivity.this, DriverEntryActivity.class);
        }
        startActivity(i);

    }
}
