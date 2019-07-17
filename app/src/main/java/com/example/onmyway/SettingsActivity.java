package com.example.onmyway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import android.net.Uri;
import android.widget.Toast;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView imgProfile;
    private EditText txtName, txtPhone, txtCar;
    private ImageView imgCross, imgSave;
    private TextView lblChangePic;

    private boolean isCustomer;
    private boolean profilePicChanged = false;

    private Uri imageUri;
    private String myUri = "";
    private StorageTask uploadTask;
    private StorageReference storageImgRef;

    private DatabaseReference dbRef;
    private FirebaseAuth auth;

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

        auth = FirebaseAuth.getInstance();
        if(isCustomer) {
            dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers");
        } else {
            dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers");
        }
        storageImgRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");

        getUserData();
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
        if (profilePicChanged) {
            saveInfoWithPic();

        } else {
            saveInfo();
        }

    }

    public void changePic(View view) {
        profilePicChanged = true;

        CropImage.activity().setAspectRatio(1, 1)
                .start(SettingsActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE &&
            resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            imgProfile.setImageURI(imageUri);
        } else {
            if (isCustomer) {
                startActivity(new Intent(SettingsActivity.this,
                        CustomerMapsActivity.class));
            } else {
                startActivity(new Intent(SettingsActivity.this,
                        DriverMapsActivity.class));
            }
            Toast.makeText(this, "Error. Please, try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveInfoWithPic() {
        if(!validation() && profilePicChanged) {
            uploadProfilePic();
        }
    }

    private void uploadProfilePic() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Saving...");
        progressDialog.show();
        if (imageUri != null) {
            final StorageReference fileRef = storageImgRef
                    .child(auth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUri = downloadUri.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", auth.getCurrentUser().getUid());
                        userMap.put("name", txtName.getText().toString());
                        userMap.put("phone", txtPhone.getText().toString());
                        userMap.put("image", myUri);

                        if (isCustomer) { // customer or driver
                            userMap.put("car", txtCar.getText().toString());
                        }
                        dbRef.child(auth.getCurrentUser().getUid()).updateChildren(userMap);

                        progressDialog.dismiss();

                        if (isCustomer) {
                            startActivity(new Intent(SettingsActivity.this,
                                    CustomerMapsActivity.class));
                        } else {
                            startActivity(new Intent(SettingsActivity.this,
                                    DriverMapsActivity.class));
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "Image is not selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveInfo() {
        if (!validation()) {
            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("uid", auth.getCurrentUser().getUid());
            userMap.put("name", txtName.getText().toString());
            userMap.put("phone", txtPhone.getText().toString());

            if (!isCustomer) { // customer or driver
                userMap.put("car", txtCar.getText().toString());
            }
            dbRef.child(auth.getCurrentUser().getUid()).updateChildren(userMap);


            if (isCustomer) {
                startActivity(new Intent(SettingsActivity.this,
                        CustomerMapsActivity.class));
            } else {
                startActivity(new Intent(SettingsActivity.this,
                        DriverMapsActivity.class));
            }
        }
    }

    private boolean validation() {
        if (TextUtils.isEmpty(txtName.getText().toString())
                || TextUtils.isEmpty(txtPhone.getText().toString())
                || (!isCustomer && TextUtils.isEmpty(txtCar.getText().toString()))) {
            Toast.makeText(this, "Please, fill in all fields", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void getUserData() {
        dbRef.child(auth.getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    txtName.setText(name);
                    txtPhone.setText(phone);

                    if (!isCustomer) {
                        String car = dataSnapshot.child("car").getValue().toString();

                        txtCar.setText(car);
                    }

                    if (dataSnapshot.hasChild("image")) {
                        String image = dataSnapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(imgProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
