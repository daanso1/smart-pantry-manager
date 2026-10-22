package com.example.smartpantrymanagement;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;

/** Settings screen: toggle for expiring-soon alerts, saved in SharedPreferences. */
public class SettingsActivity extends BaseNavActivity {

    public static final String PREFS = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SwitchCompat sw = findViewById(R.id.switch_expiry);
        sw.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        sw.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, checked).apply();
            Toast.makeText(this, checked ? "Expiry alerts on" : "Expiry alerts off",
                    Toast.LENGTH_SHORT).show();
        });

        setupBottomNav(R.id.nav_settings);
    }
}
