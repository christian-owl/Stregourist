package com.example.stregourist;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

public class NotificationSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    public static class SettingsFragment extends PreferenceFragmentCompat {
        private SwitchPreferenceCompat allAlerts;
        private SwitchPreferenceCompat proximityAlert;
        private SwitchPreferenceCompat dailyAlert;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            allAlerts = findPreference("alerts");
            proximityAlert = findPreference("proximity_alert");
            dailyAlert = findPreference("daily_tips");

            if (allAlerts != null) {
                updateChildPreferences(allAlerts.isChecked());
                allAlerts.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isEnabled = (boolean) newValue;
                    updateChildPreferences(isEnabled);
                    return true;
                });
            }
        }

        private void updateChildPreferences(boolean enabled) {
            if (proximityAlert != null) {
                proximityAlert.setEnabled(enabled);
            }
            if (dailyAlert != null) {
                dailyAlert.setEnabled(enabled);
            }
        }
    }

}