package com.termux.app.activities;

import android.os.Bundle;

import androidx.activity.ComponentActivity;

import com.termux.app.tv.TvSettingsHost;

public class SettingsActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TvSettingsHost.attach(this);
    }
}
