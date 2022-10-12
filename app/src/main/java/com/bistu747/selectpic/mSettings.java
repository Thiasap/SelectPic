package com.bistu747.selectpic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bistu747.selectpic.Fragment.SettingsFragment;

public class mSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msettings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }
}