package com.example.taxiappswm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CustomerLoginActivity extends AppCompatActivity {

    TextView driverStatus, question;
    EditText driverEmail, driverPassword;
    Button signIn, Register;

    FirebaseAuth mAuth;
    DatabaseReference CustomerDatabaseRef, CustomerDatabaseIdExists;
    ProgressDialog loadingBar;
    String OnlineCustomerId, customerCheckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);

        /*element page*/
        driverStatus = (TextView) findViewById(R.id.statusPas);
        question = (TextView) findViewById(R.id.question);
        driverEmail = (EditText) findViewById(R.id.pasEmail);
        driverPassword = (EditText) findViewById(R.id.pasPassword);
        signIn = (Button) findViewById(R.id.signIn);
        Register = (Button) findViewById(R.id.Register);
        /*element page*/

        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

       /* signIn.setVisibility(View.INVISIBLE);
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

                LoginCustomer(email, password);

            }
        });
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = driverEmail.getText().toString();
                String password = driverPassword.getText().toString();

                RegisterCustomer(email, password);

            }
        });

    }

    private void LoginCustomer(String email, String password) {
        loadingBar.setTitle("Вход пассажира.");
        loadingBar.setMessage("Дождитесь загрузки.");
        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(CustomerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            customerCheckId = mAuth.getCurrentUser().getUid();

                            DatabaseReference CustomerDatabaseIdExists = FirebaseDatabase.getInstance().getReference().child("Users").child("customers");
                            CustomerDatabaseIdExists.child(customerCheckId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists() && snapshot.getChildrenCount() > 0) {
                                        Toast.makeText(CustomerLoginActivity.this, "Успешный вход" , Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                        Intent customerIntent = new Intent(CustomerLoginActivity.this, CustomerMapsActivity.class);
                                        startActivity(customerIntent);
                                    } else {
                                        Intent customerIntent = new Intent(CustomerLoginActivity.this, WelcomeActivity.class);
                                        startActivity(customerIntent);
                                        Toast.makeText(CustomerLoginActivity.this, "Пользователь но не в таблице пользователей" , Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                         /*   DatabaseReference CustomerDatabaseIdExists = FirebaseDatabase.getInstance().getReference();
                            Query query = CustomerDatabaseIdExists.child("Users").orderByChild("customers").equalTo(customerCheckId);

                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Toast.makeText(CustomerLoginActivity.this, "Успешный вход пользователя" + customerCheckId, Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(CustomerLoginActivity.this, "Такой пользователь не найден" + customerCheckId, Toast.LENGTH_SHORT).show();
                                }
                            });

*/
                             /*   customerCheckId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                CustomerDatabaseIdExists = FirebaseDatabase.getInstance().getReference()
                                            .child("Users").orderByChild("customers").equalTo(customerCheckId);
                                            //.child("Users").child("customers").child(customerCheckId);
                            Toast.makeText(CustomerLoginActivity.this, "данные пользователя CustomerDatabaseIdExists" + CustomerDatabaseIdExists, Toast.LENGTH_SHORT).show();
                                if(CustomerDatabaseIdExists != null) {
                                    Toast.makeText(CustomerLoginActivity.this, "Успешный вход", Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();

                                    Intent customerIntent = new Intent(CustomerLoginActivity.this, CustomerMapsActivity.class);
                                    startActivity(customerIntent);
                                } else {
                                    Toast.makeText(CustomerLoginActivity.this, "Пользователя с такими данными обнаружить не удалось", Toast.LENGTH_SHORT).show();
                                }
*/

                        } else {
                            Toast.makeText(CustomerLoginActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void RegisterCustomer(String email, String password) {
        loadingBar.setTitle("Регистрация пассажира.");
        loadingBar.setMessage("Дождитесь загрузки.");
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    OnlineCustomerId = mAuth.getCurrentUser().getUid();
                    CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child("Customers").child(OnlineCustomerId);
                    CustomerDatabaseRef.setValue(true);

                    Intent customerIntent = new Intent(CustomerLoginActivity.this, CustomerMapsActivity.class);
                    startActivity(customerIntent);


                    Toast.makeText(CustomerLoginActivity.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    Toast.makeText(CustomerLoginActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }
}