package com.example.onmyway;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView imgProfile;
    private EditText txtName, txtPhone, txtCar;
    private ImageView imgCross, imgSave;
    private TextView lblChangePic;

    private boolean isCustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imgProfile = findViewById(R.id.profile_image);

        txtName = findViewById(R.id.txtName);
        txtPhone = findViewById(R.id.txtPhone);
        txtCar = findViewById(R.id.txtCar);
        //imgCross = findViewById(R.id.imgCross);

        lblChangePic = findViewById(R.id.lblChangePic);

        //set title on create
        Bundle extras = getIntent().getExtras();
        isCustomer = extras.getBoolean("isCustomer");

        if (!isCustomer) {
            txtCar.setVisibility(View.VISIBLE);
        }
    }

    public void close(View view) {
        Intent i;
        if (isCustomer) {
            i = new Intent(SettingsActivity.this, CustomerMapsActivity.class);
        } else {
            i = new Intent(SettingsActivity.this, DriverMapsActivity.class);
        }
        startActivity(i);
    }

    public void save(View view) {

    }
}
