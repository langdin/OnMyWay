package com.example.onmyway;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
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

    private void goToActivity(boolean isCustomer) {
        Intent i = new Intent(WelcomeActivity.this, EntryActivity.class);
        i.putExtra("isCustomer", isCustomer);
        /*if(isCustomer) {
            //i = new Intent(WelcomeActivity.this, CustomerEntryActivity.class);
            i.putExtra("", isCustomer);
        } else {
            //i = new Intent(WelcomeActivity.this, DriverEntryActivity.class);
        }*/
        startActivity(i);

    }
}
