package com.bistu747.selectpic.Fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.bistu747.selectpic.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

    }
}