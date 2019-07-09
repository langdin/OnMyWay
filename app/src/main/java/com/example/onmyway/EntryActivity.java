package com.example.onmyway;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;

public class EntryActivity extends AppCompatActivity {
    private View gif;
    private Button btnEntry;
    private TextView lblEntryToggle;
    private TextView lblMain;
    private boolean isCustomer;
    private boolean isLogin;
    private EditText txtEmail;
    private EditText txtPassword;

    private FirebaseAuth auth;

    private ProgressDialog loadBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        btnEntry = (Button)findViewById(R.id.btnEntry);
        lblEntryToggle = (TextView) findViewById(R.id.lblEntryToggle);
        lblMain = (TextView) findViewById(R.id.lblMainLbl);
        gif = findViewById(R.id.gif);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        isLogin = true;

        auth = FirebaseAuth.getInstance();

        loadBar = new ProgressDialog(this);

        //set title on create
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
            // Login
            btnEntry.setText("Login");
            lblEntryToggle.setText("Don't have an account?");
            if (isCustomer) {
                // Login for customer title
                lblMain.setText("Customer Login");
            } else {
                //Login for driver title
                lblMain.setText("Driver Login");
            }
        } else {
            // Register
            btnEntry.setText("Register");
            lblEntryToggle.setText("Go to Login");
            if (isCustomer) {
                // Register for customer title
                lblMain.setText("Customer Registration");
            } else {
                // Register for driver title
                lblMain.setText("Driver Registration");
            }
        }

    }

    public void entry(View view) {

        String email = txtEmail.getText().toString();
        String password = txtPassword.getText().toString();

        if (isCustomer) {
            if (isLogin) {
                // Login for customer
                login(email, password);
            } else {
                // Register for customer
                register(email, password);
            }
        } else {
            if (isLogin) {
                // Login for driver
                login(email, password);
            } else {
                // Register for driver
                register(email, password);
            }
        }

    }

    // TODO: add separate registrations for driver & customer
    private void register(String email, String password) {
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(EntryActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else {

            loadBar.setTitle("Registration");
            loadBar.setMessage("Please wait...");
            loadBar.show();

            // create user
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                loadBar.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(EntryActivity.this, "Successfully registered.", Toast.LENGTH_SHORT).show();
                    //go to next activity
                    goToDriverMap();
                } else {
                    Toast.makeText(EntryActivity.this, "Registration was unsuccessful. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // TODO: add separate Logins for driver & customer
    private void login(String email, String password) {
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(EntryActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        } else {

            loadBar.setTitle("Signing In");
            loadBar.setMessage("Please wait...");
            loadBar.show();

            // create user
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                loadBar.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(EntryActivity.this, "Successfully signed in.", Toast.LENGTH_SHORT).show();

                    //go to next activity
                    goToDriverMap();
                } else {
                    Toast.makeText(EntryActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void goToDriverMap() {
        Intent i = new Intent(EntryActivity.this, DriverMapsActivity.class);
        startActivity(i);
    }

}