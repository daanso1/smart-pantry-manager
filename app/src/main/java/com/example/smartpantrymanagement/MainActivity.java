package com.example.smartpantrymanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * Pantry List screen (launcher). Shows every pantry item from SQLite in a
 * RecyclerView. Tap a row to edit, tap the bin to delete, tap + to add.
 */
public class MainActivity extends BaseNavActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView rv;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        rv = findViewById(R.id.rv_pantry);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new PantryAdapter(new ArrayList<>(), true, new PantryAdapter.Listener() {
            @Override
            public void onEdit(PantryItem item) {
                // Explicit Intent carrying the item id to the Add/Edit screen
                Intent i = new Intent(MainActivity.this, AddEditItemActivity.class);
                i.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(PantryItem item) {
                confirmDelete(item);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));

        setupBottomNav(R.id.nav_pantry);
    }

    // Reload the list every time the screen comes back to the foreground,
    // so edits made on other screens appear immediately.
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
        boolean alerts = prefs.getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true);
        adapter.setItems(db.getAllPantryItems(), alerts);

        boolean empty = adapter.getItemCount() == 0;
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(final PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.deletePantryItem(item.getId());
                    Toast.makeText(this, item.getName() + " deleted", Toast.LENGTH_SHORT).show();
                    loadItems();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}