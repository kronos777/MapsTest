package com.example.taxiappswm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    Button customerBtn, driverBtn, testMap, phoneAuthCustom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        customerBtn = (Button) findViewById(R.id.customerBtn);
        driverBtn = (Button) findViewById(R.id.driverBtn);
        testMap = (Button) findViewById(R.id.testbtnmap);
        phoneAuthCustom = (Button) findViewById(R.id.phoneAuthCustom);

        testMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customerIntent = new Intent(WelcomeActivity.this, TestMapsActivity.class);
                startActivity(customerIntent);
            }
        });

        driverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent driverIntent = new Intent(WelcomeActivity.this, DriverLoginActivity.class);
                startActivity(driverIntent);
            }
        });


        customerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customerIntent = new Intent(WelcomeActivity.this, CustomerLoginActivity.class);
                startActivity(customerIntent);
            }
        });

        phoneAuthCustom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent customerIntent = new Intent(WelcomeActivity.this, PhoneAuthActivity.class);
                startActivity(customerIntent);
            }
        });

    }

    //startActivity(new Intent(MainActivity.this, TestMapsActivity.class));
}