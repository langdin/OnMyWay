package com.example.onmyway;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EntryActivity extends AppCompatActivity {
    private View gif;
    private Button btnEntry;
    private TextView lblEntryToggle;
    private TextView lblMain;
    private boolean isCustomer;
    private boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        btnEntry = (Button)findViewById(R.id.btnEntry);
        lblEntryToggle = (TextView) findViewById(R.id.lblEntryToggle);
        lblMain = (TextView) findViewById(R.id.lblMainLbl);
        gif = findViewById(R.id.gif);

        isLogin = true;


        Bundle extras = getIntent().getExtras();
        isCustomer = extras.getBoolean("isCustomer");
        if (isCustomer) {
            lblMain.setText("Customer Login");
            gif.setBackgroundResource(R.drawable.client);
        } else {
            lblMain.setText("Driver Login");
            gif.setBackgroundResource(R.drawable.driver);
        }
    }

    public void entryToggle(View view) {

        isLogin = !isLogin;

        if (isLogin) {
            lblEntryToggle.setText("Don't have an account?");
            if (isCustomer) {
                lblMain.setText("Customer Login");
            } else {
                lblMain.setText("Driver Login");
            }
        } else {
            lblEntryToggle.setText("Go to Login");
            if (isCustomer) {
                lblMain.setText("Customer Registration");
            } else {
                lblMain.setText("Driver Registration");
            }
        }

    }

    public void entry(View view) {
        if (isCustomer) {

        } else {

        }

    }

}
