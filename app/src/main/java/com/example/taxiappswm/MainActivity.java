package com.example.taxiappswm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread = new Thread() {
            @Override
            public void run(){
                try {
                    sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //startActivity(new Intent(MainActivity.this, TestMapsActivity.class));
                    startActivity(new Intent(MainActivity.this, WelcomeActivity.class));
                }
            }
        };

        thread.start();



    }

    protected void onPause() {
        super.onPause();

        finish();
    }

}