package com.example.smartpantrymanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen. Runs the strict-matching rule against the
 * current pantry and lists ONLY recipes the user can make right now.
 */
public class SuggestedRecipesActivity extends BaseNavActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView rv;
    private TextView tvEmpty, tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        db = new DatabaseHelper(this);
        rv = findViewById(R.id.rv_recipes);
        tvEmpty = findViewById(R.id.tv_empty);
        tvCount = findViewById(R.id.tv_count);

        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        setupBottomNav(R.id.nav_suggested);
    }

    // Re-run the matching every time the screen is shown so the list always
    // reflects the current pantry.
    @Override
    protected void onResume() {
        super.onResume();
        List<Recipe> suggested = IngredientMatcher.getSuggestedRecipes(
                db.getAllRecipes(), db.getAllPantryItems());
        adapter.setRecipes(suggested);

        boolean none = suggested.isEmpty();
        tvEmpty.setVisibility(none ? View.VISIBLE : View.GONE);
        rv.setVisibility(none ? View.GONE : View.VISIBLE);
        tvCount.setText(none ? "" : suggested.size() + " recipe(s) you can make right now");
    }
}
