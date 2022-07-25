package com.faruq.reesit.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.faruq.reesit.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding activitySettingsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySettingsBinding = ActivitySettingsBinding.inflate(LayoutInflater.from(this));
        setContentView(activitySettingsBinding.getRoot());
    }
}
