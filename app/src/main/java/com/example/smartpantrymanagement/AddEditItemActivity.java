package com.example.smartpantrymanagement;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Calendar;
import java.util.Locale;

/**
 * Add / Edit Ingredient screen. If the launching Intent carries an
 * "item_id" extra the screen edits that item, otherwise it creates a new one.
 */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    private static final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "pcs"};

    private DatabaseHelper db;
    private EditText etName, etQuantity, etExpiry;
    private Spinner spUnit;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        db = new DatabaseHelper(this);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.et_name);
        etQuantity = findViewById(R.id.et_quantity);
        etExpiry = findViewById(R.id.et_expiry);
        spUnit = findViewById(R.id.sp_unit);
        Button btnSave = findViewById(R.id.btn_save);
        Button btnClear = findViewById(R.id.btn_clear_expiry);

        spUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS));

        etExpiry.setOnClickListener(v -> showDatePicker());
        btnClear.setOnClickListener(v -> etExpiry.setText(""));
        btnSave.setOnClickListener(v -> save());

        // Edit mode: pre-fill the form from the database
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            PantryItem item = db.getPantryItem(itemId);
            if (item != null) {
                toolbar.setTitle("Edit Ingredient");
                etName.setText(item.getName());
                etQuantity.setText(IngredientMatcher.formatQty(item.getQuantity()));
                etExpiry.setText(item.getExpiryDate());
                for (int i = 0; i < UNITS.length; i++) {
                    if (UNITS[i].equals(item.getUnit())) spUnit.setSelection(i);
                }
            } else {
                itemId = -1;
            }
        }
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) ->
                etExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form, then inserts (Create) or updates (Update) the item. */
    private void save() {
        String name = etName.getText().toString().trim();
        String qtyText = etQuantity.getText().toString().trim();
        String expiry = etExpiry.getText().toString().trim();
        boolean valid = true;

        if (name.isEmpty()) {
            etName.setError("Ingredient name is required");
            valid = false;
        } else if (!name.matches("[A-Za-z][A-Za-z \\-]+")) {
            etName.setError("Use letters only (at least 2)");
            valid = false;
        }

        double qty = 0;
        if (qtyText.isEmpty()) {
            etQuantity.setError("Quantity is required");
            valid = false;
        } else {
            try {
                qty = Double.parseDouble(qtyText);
                if (qty <= 0) {
                    etQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                etQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) return;

        String unit = spUnit.getSelectedItem().toString();
        if (itemId == -1) {
            db.addPantryItem(new PantryItem(0, name, qty, unit, expiry));
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            db.updatePantryItem(new PantryItem(itemId, name, qty, unit, expiry));
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
