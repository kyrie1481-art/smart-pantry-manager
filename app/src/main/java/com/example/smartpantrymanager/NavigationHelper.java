package com.example.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Shared navigation code so every main screen behaves the same way.
 */
public class NavigationHelper {

    /**
     * Keeps content away from the status bar, the system navigation bar and the keyboard
     * (needed because newer Android versions draw apps edge-to-edge).
     */
    public static void applyInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Wires up the bottom navigation bar. Each tap uses an Intent to open the chosen screen.
     * The Pantry screen is the "home" screen: the other two main screens sit on top of it,
     * so pressing Back always returns to the pantry.
     */
    public static void setupBottomNav(final Activity activity, final int selectedItemId) {
        BottomNavigationView nav = activity.findViewById(R.id.bottom_nav);
        nav.setSelectedItemId(selectedItemId);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) {
                return true; // already on this screen
            }

            if (id == R.id.nav_pantry) {
                // Go back to the existing pantry screen and close anything above it
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.startActivity(intent);
            } else {
                Class<?> target = (id == R.id.nav_suggested)
                        ? SuggestedRecipesActivity.class
                        : SettingsActivity.class;
                Intent intent = new Intent(activity, target);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.startActivity(intent);
                // Replace the current non-home screen so the back stack stays short
                if (!(activity instanceof MainActivity)) {
                    activity.finish();
                }
            }
            return true;
        });
    }
}
