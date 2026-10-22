package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 1: the Pantry List (home screen).
 * Shows every ingredient from the database in a RecyclerView and lets the user
 * add (+ button), edit (tap a card) and delete (bin icon) items.
 */
public class MainActivity extends AppCompatActivity implements PantryAdapter.OnItemActionListener {

    private DatabaseHelper dbHelper;
    private PantryAdapter adapter;
    private TextView tvEmpty;
    private TextView tvExpiryBanner;

    /** Called once when the screen is created: find views and set everything up. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavigationHelper.applyInsets(findViewById(R.id.root));

        dbHelper = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvExpiryBanner = findViewById(R.id.tvExpiryBanner);

        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(new ArrayList<PantryItem>(), this);
        recycler.setAdapter(adapter);

        // The + button opens the Add/Edit screen using an Intent (no extras = "add mode")
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddEditItemActivity.class)));

        NavigationHelper.setupBottomNav(this, R.id.nav_pantry);
    }

    /** Called every time the screen comes back into view, so the list is always up to date. */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    /** Reads the pantry from the database and shows it. */
    private void loadPantry() {
        List<PantryItem> items = dbHelper.getAllPantryItems();
        boolean alertsOn = SettingsActivity.isAlertsEnabled(this);
        adapter.setItems(items, alertsOn);

        // Friendly message instead of a blank screen
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);

        // Count the ingredients that are expiring soon (only if alerts are switched on)
        int expiringCount = 0;
        if (alertsOn) {
            for (PantryItem item : items) {
                if (AppUtils.isExpiringSoon(item.getExpiryDate())) {
                    expiringCount++;
                }
            }
        }
        if (expiringCount > 0) {
            String verb = expiringCount == 1 ? " ingredient is" : " ingredients are";
            tvExpiryBanner.setText(expiringCount + verb + " expiring within "
                    + AppUtils.EXPIRY_WARNING_DAYS + " days (or already expired). Cook them first!");
            tvExpiryBanner.setVisibility(View.VISIBLE);
        } else {
            tvExpiryBanner.setVisibility(View.GONE);
        }
    }

    /** Card tapped: open the Add/Edit screen and pass the item's id inside the Intent. */
    @Override
    public void onEdit(PantryItem item) {
        Intent intent = new Intent(this, AddEditItemActivity.class);
        intent.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    /** Bin icon tapped: ask for confirmation, then delete from the database. */
    @Override
    public void onDelete(final PantryItem item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePantryItem(item.getId());
                    loadPantry();
                    Toast.makeText(MainActivity.this, "Ingredient deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
