package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Screen 5: Settings.
 * Two preferences, saved with SharedPreferences so they survive closing the app:
 *  - expiring-soon alerts (on / off)
 *  - the default unit pre-selected on the Add Ingredient screen
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "pantry_settings";
    private static final String KEY_ALERTS = "alerts_enabled";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    /** Other screens call this to find out whether expiry alerts are switched on. */
    public static boolean isAlertsEnabled(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ALERTS, true);
    }

    /** Other screens call this to find out which unit should be pre-selected. */
    public static String getDefaultUnit(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_DEFAULT_UNIT, "g");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        NavigationHelper.applyInsets(findViewById(R.id.root));

        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // --- alerts switch ---
        SwitchMaterial switchAlerts = findViewById(R.id.switchAlerts);
        switchAlerts.setChecked(prefs.getBoolean(KEY_ALERTS, true));
        switchAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean(KEY_ALERTS, isChecked).apply());

        // --- default unit drop-down ---
        Spinner spDefaultUnit = findViewById(R.id.spDefaultUnit);
        ArrayAdapter<String> unitAdapter =
                new ArrayAdapter<>(this, R.layout.item_spinner, AppUtils.UNITS);
        unitAdapter.setDropDownViewResource(R.layout.item_spinner);
        spDefaultUnit.setAdapter(unitAdapter);

        String savedUnit = prefs.getString(KEY_DEFAULT_UNIT, "g");
        for (int i = 0; i < AppUtils.UNITS.length; i++) {
            if (AppUtils.UNITS[i].equals(savedUnit)) {
                spDefaultUnit.setSelection(i);
            }
        }

        spDefaultUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putString(KEY_DEFAULT_UNIT, AppUtils.UNITS[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // nothing to do
            }
        });

        NavigationHelper.setupBottomNav(this, R.id.nav_settings);
    }
}
