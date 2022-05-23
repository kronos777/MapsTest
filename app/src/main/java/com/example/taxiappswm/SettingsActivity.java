package com.example.taxiappswm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private String getType;

    private CircleImageView circleImageView;
    private EditText nameET, phoneET, carET;
    private ImageView closeBtn, saveBtn;
    private TextView imageChangeBtn;
    private String checker = "";
    private Uri imageUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfileImageRef;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getType = getIntent().getStringExtra("type");


        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(getType);
        storageProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");

        circleImageView = (CircleImageView) findViewById(R.id.profile_image);
        nameET = (EditText) findViewById(R.id.name);
        phoneET = (EditText) findViewById(R.id.phone);
        carET = (EditText) findViewById(R.id.carModel);

        if (getType.equals("drivers")) {
            carET.setVisibility(View.VISIBLE);
       }


        closeBtn = (ImageView) findViewById(R.id.close_button);
        saveBtn = (ImageView) findViewById(R.id.close_save);
        imageChangeBtn = (TextView) findViewById(R.id.change_foto);

        getUserInformation();

    }

    public void clickExitSettings(View view) {

        if(getType.equals("customers")) {
            Intent intent = new Intent(SettingsActivity.this, CustomerMapsActivity.class);
            startActivity(intent);
        } else if (getType.equals("drivers")) {
            Intent intent = new Intent(SettingsActivity.this, DriverMapsActivity.class);
            startActivity(intent);
        }

    }

    public void clickSaveSettings(View view) {
        if(checker.equals("clicked")){
            validateControllers();
        } else {
            validateSaveOnlyInformation();
        }
    }

    private void validateSaveOnlyInformation() {
        if (TextUtils.isEmpty(nameET.getText().toString())){
            Toast.makeText(this, "Заполните поле Имя", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneET.getText().toString())){
            Toast.makeText(this, "Заполните поле Телефон", Toast.LENGTH_SHORT).show();
        } else if (getType.equals("Drivers") && TextUtils.isEmpty(carET.getText().toString())){
            Toast.makeText(this, "Заполните поле Авто", Toast.LENGTH_SHORT).show();
        }

        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("uid", mAuth.getCurrentUser().getUid());
        userMap.put("name", nameET.getText().toString());
        userMap.put("phone", phoneET.getText().toString());


        if (getType.equals("drivers")) {
            userMap.put("car", carET.getText().toString());
        }

        databaseReference.child(mAuth.getCurrentUser().getUid())
                .updateChildren(userMap);


        if (getType.equals("drivers")) {
            startActivity(new Intent(SettingsActivity.this, DriverMapsActivity.class));
        } else {
            startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
        }

    }


    public void checkImageOne(View view) {

        checker = "clicked";

        CropImage.activity().setAspectRatio(1,1)
                .start(SettingsActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK &&
           data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            circleImageView.setImageURI(imageUri);
        } else {
            if (getType.equals("Drivers")) {
                startActivity(new Intent(SettingsActivity.this, DriverMapsActivity.class));
            } else {
                startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
            }
            Toast.makeText(this, "Произошла ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    private void validateControllers() {
        if (TextUtils.isEmpty(nameET.getText().toString())){
            Toast.makeText(this, "Заполните поле Имя", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(phoneET.getText().toString())){
            Toast.makeText(this, "Заполните поле Телефон", Toast.LENGTH_SHORT).show();
        } else if (getType.equals("Drivers") && TextUtils.isEmpty(carET.getText().toString())){
            Toast.makeText(this, "Заполните поле Авто", Toast.LENGTH_SHORT).show();
        } else if (checker.equals("clicked")) {
            uploadProfileImage();
        }
    }

    private void uploadProfileImage() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Загрузка информации");
        progressDialog.setMessage("Пожалуйста подождите");
        progressDialog.show();


        if(imageUri != null){
            final StorageReference fileRef = storageProfileImageRef.child(mAuth.getCurrentUser().getUid() + ".jpg");
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    //return null;
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }

                    return fileRef.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task <Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();

                        HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("uid", mAuth.getCurrentUser().getUid());
                        userMap.put("name", nameET.getText().toString());
                        userMap.put("phone", phoneET.getText().toString());
                        userMap.put("image", myUrl);

                        if (getType.equals("Drivers")) {
                            userMap.put("car", carET.getText().toString());
                        }

                        databaseReference.child(mAuth.getCurrentUser().getUid())
                                .updateChildren(userMap);

                        progressDialog.dismiss();

                        if (getType.equals("drivers")) {
                            startActivity(new Intent(SettingsActivity.this, DriverMapsActivity.class));
                        } else {
                            startActivity(new Intent(SettingsActivity.this, CustomerMapsActivity.class));
                        }

                    }
                }
            });

        } else {
            Toast.makeText(this, "Изображение не выбрано", Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserInformation() {
        databaseReference.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();

                    if (getType.equals("Drivers")) {
                        String car = snapshot.child("car").getValue().toString();
                        carET.setText(car);
                    }

                    nameET.setText(name);
                    phoneET.setText(phone);

                    if(snapshot.hasChild("image")) {
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(circleImageView);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
