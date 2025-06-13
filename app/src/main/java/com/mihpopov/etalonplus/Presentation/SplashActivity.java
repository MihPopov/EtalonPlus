package com.mihpopov.etalonplus.Presentation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
        finishAffinity();
    }
}