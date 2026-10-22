package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

/**
 * Screen 2: Add / Edit Ingredient.
 * The same screen is used for both jobs. If the Intent contains an item id we are in
 * "edit mode" (fields are pre-filled); otherwise we are in "add mode".
 * All input is validated before anything is saved.
 */
public class AddEditItemActivity extends AppCompatActivity {

    /** Key used to pass the item id from the pantry list to this screen via an Intent. */
    public static final String EXTRA_ITEM_ID = "item_id";

    private TextInputLayout tilName;
    private TextInputLayout tilQuantity;
    private TextInputEditText etName;
    private TextInputEditText etQuantity;
    private TextInputEditText etExpiry;
    private Spinner spUnit;
    private TextView tvFormTitle;
    private DatabaseHelper dbHelper;

    private int editingItemId = -1; // -1 means "adding a new item"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);
        NavigationHelper.applyInsets(findViewById(R.id.root));

        dbHelper = new DatabaseHelper(this);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        tilName = findViewById(R.id.tilName);
        tilQuantity = findViewById(R.id.tilQuantity);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);
        MaterialButton btnSave = findViewById(R.id.btnSave);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        MaterialButton btnClearDate = findViewById(R.id.btnClearDate);

        // Fill the unit drop-down (g, kg, ml, l, pcs, cup, tbsp, tsp)
        ArrayAdapter<String> unitAdapter =
                new ArrayAdapter<>(this, R.layout.item_spinner, AppUtils.UNITS);
        unitAdapter.setDropDownViewResource(R.layout.item_spinner);
        spUnit.setAdapter(unitAdapter);

        // Did the pantry list send us an item id? Then we are editing.
        editingItemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (editingItemId != -1) {
            loadItemForEditing();
        } else {
            // Adding: pre-select the unit chosen in Settings
            spUnit.setSelection(indexOfUnit(SettingsActivity.getDefaultUnit(this)));
        }

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnClearDate.setOnClickListener(v -> etExpiry.setText(""));
        btnSave.setOnClickListener(v -> saveItem());
        btnCancel.setOnClickListener(v -> finish());
    }

    /** Edit mode: read the item from the database and put its values into the form. */
    private void loadItemForEditing() {
        PantryItem item = dbHelper.getPantryItem(editingItemId);
        if (item == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvFormTitle.setText("Edit Ingredient");
        etName.setText(item.getName());
        etQuantity.setText(AppUtils.formatQuantity(item.getQuantity()));
        etExpiry.setText(item.getExpiryDate());
        spUnit.setSelection(indexOfUnit(item.getUnit()));
    }

    /** Opens a calendar so the user can pick an expiry date. */
    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        new DatePickerDialog(this,
                (picker, year, month, day) -> etExpiry.setText(
                        String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form and, if everything is fine, saves (insert or update) and closes. */
    private void saveItem() {
        String name = readText(etName).trim();
        String quantityText = readText(etQuantity).trim();
        String unit = spUnit.getSelectedItem().toString();
        String expiry = readText(etExpiry).trim();

        // Clear old error messages first
        tilName.setError(null);
        tilQuantity.setError(null);

        boolean valid = true;

        // --- validate the name ---
        if (name.isEmpty()) {
            tilName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches("[\\p{L}][\\p{L} '\\-]*")) {
            tilName.setError("Use letters only (e.g. Tomatoes)");
            valid = false;
        }

        // --- validate the quantity ---
        double quantity = 0;
        if (quantityText.isEmpty()) {
            tilQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                // Some phones type a comma as the decimal sign, so swap it for a dot
                quantity = Double.parseDouble(quantityText.replace(',', '.'));
                if (quantity <= 0) {
                    tilQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                } else if (quantity > 100000) {
                    tilQuantity.setError("That quantity is too large");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) {
            return; // stay on the screen so the user can fix the errors
        }

        if (editingItemId == -1) {
            dbHelper.addPantryItem(new PantryItem(0, name, quantity, unit, expiry));   // CREATE
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updatePantryItem(new PantryItem(editingItemId, name, quantity, unit, expiry)); // UPDATE
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish(); // go back to the pantry list, which reloads itself in onResume
    }

    /** Safely reads the text of an input box (never null). */
    private String readText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    /** Position of a unit inside the drop-down (0 if not found). */
    private int indexOfUnit(String unit) {
        for (int i = 0; i < AppUtils.UNITS.length; i++) {
            if (AppUtils.UNITS[i].equals(unit)) {
                return i;
            }
        }
        return 0;
    }
}
