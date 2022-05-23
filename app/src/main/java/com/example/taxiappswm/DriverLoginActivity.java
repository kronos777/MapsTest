package com.example.taxiappswm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DriverLoginActivity extends AppCompatActivity {


    TextView driverStatus, question;
    EditText driverEmail, driverPassword;
    Button signIn, Register;

    FirebaseAuth mAuth;

    ProgressDialog loadingBar;

    DatabaseReference DriverDatabaseRef, driverDatabaseIdExists;
    String OnlineDriverId, driverCheckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        /*element page*/
        driverStatus = (TextView) findViewById(R.id.statusDriver);
        question = (TextView) findViewById(R.id.question);
        driverEmail = (EditText) findViewById(R.id.driverEmail);
        driverPassword = (EditText) findViewById(R.id.driverPassword);
        signIn = (Button) findViewById(R.id.signIn);
        Register = (Button) findViewById(R.id.Register);
        /*element page*/

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        /*signIn.setVisibility(View.INVISIBLE);
        signIn.setEnabled(false);

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverStatus.setText("Регистрация для водителей.");
            }
        });
*/

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = driverEmail.getText().toString();
                String password = driverPassword.getText().toString();

                LoginDriver(email, password);

            }
        });


        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = driverEmail.getText().toString();
                String password = driverPassword.getText().toString();

                RegisterDriver(email, password);

            }
        });

    }

    private void LoginDriver(String email, String password) {
        loadingBar.setTitle("Вход водителя.");
        loadingBar.setMessage("Дождитесь загрузки.");
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            driverCheckId = mAuth.getCurrentUser().getUid();

                            DatabaseReference CustomerDatabaseIdExists = FirebaseDatabase.getInstance().getReference().child("Users").child("drivers");
                            CustomerDatabaseIdExists.child(driverCheckId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists() && snapshot.getChildrenCount() > 0) {
                                        Toast.makeText(DriverLoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                        Intent driverIntent = new Intent(DriverLoginActivity.this, DriverMapsActivity.class);
                                        startActivity(driverIntent);
                                    } else {
                                        Intent customerIntent = new Intent(DriverLoginActivity.this, WelcomeActivity.class);
                                        startActivity(customerIntent);
                                        Toast.makeText(DriverLoginActivity.this, "Пользователь но не в таблице водителей" , Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });





                        } else {
                            Toast.makeText(DriverLoginActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void RegisterDriver(String email, String password) {
        loadingBar.setTitle("Регистрация водителя.");
        loadingBar.setMessage("Дождитесь загрузки.");
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            OnlineDriverId = mAuth.getCurrentUser().getUid();
                            DriverDatabaseRef = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child("Drivers").child(OnlineDriverId);
                            DriverDatabaseRef.setValue(true);

                            Intent driverIntent = new Intent(DriverLoginActivity.this, DriverMapsActivity.class);
                            startActivity(driverIntent);

                            Toast.makeText(DriverLoginActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        } else {
                            Toast.makeText(DriverLoginActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });     /**/
    }

}