package com.example.onmyway;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CustomerEntryActivity extends AppCompatActivity {

    private Button btnLogin;
    private Button btnRegister;
    private TextView lblGoToRegister;
    private TextView lblMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_entry);

        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnRegister = (Button)findViewById(R.id.btnRegister);
        lblGoToRegister = (TextView) findViewById(R.id.lblGoToRegister);
        lblMain = (TextView) findViewById(R.id.lblMainLbl);

        btnLogin.setEnabled(true);
        btnLogin.setVisibility(View.VISIBLE);

        btnRegister.setVisibility(View.INVISIBLE);
        btnRegister.setEnabled(false);
    }

    public void goToRegister(View view) {
        btnLogin.setVisibility(View.INVISIBLE);
        btnLogin.setEnabled(false);

        btnRegister.setEnabled(true);
        btnRegister.setVisibility(View.VISIBLE);

        lblGoToRegister.setVisibility(View.INVISIBLE);
        lblGoToRegister.setEnabled(false);

        lblMain.setText("Customer Register");

    }

    public void login(View view) {

    }

    public void register(View view) {

    }
}
