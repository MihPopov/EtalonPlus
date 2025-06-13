package com.mihpopov.etalonplus.Presentation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.mihpopov.etalonplus.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_green));
    }

    public void onCheckCardClick(View view) {
        startActivity(new Intent(HomeActivity.this, CheckActivity.class));
    }

    public void onStorageCardClick(View view) {
        startActivity(new Intent(HomeActivity.this, StorageActivity.class));
    }

    public void onSettingsCardClick(View view) {
        startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
    }

    public void onFeedbackCardClick(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.yandex.ru/u/67e16e2050569081bd96015f/")));
    }
}