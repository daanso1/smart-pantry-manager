package com.example.smartpantrymanagement;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Parent class for the three top-level screens (Pantry, Recipes, Settings).
 * Wires up the bottom navigation bar using explicit Intents.
 */
public abstract class BaseNavActivity extends AppCompatActivity {

    protected void setupBottomNav(final int selectedId) {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedId) return true;

            Intent intent;
            if (id == R.id.nav_pantry) {
                intent = new Intent(this, MainActivity.class);
                // Reuse the existing pantry screen instead of stacking a new one
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            } else if (id == R.id.nav_suggested) {
                intent = new Intent(this, SuggestedRecipesActivity.class);
            } else {
                intent = new Intent(this, SettingsActivity.class);
            }
            startActivity(intent);
            overridePendingTransition(0, 0);
            if (selectedId != R.id.nav_pantry) finish(); // keep the back stack tidy
            return true;
        });
    }
}
