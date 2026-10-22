package com.example.smartpantrymanagement;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/** Recipe Detail screen: full ingredient list and method for one recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long id = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(id);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar.setTitle(recipe.getName());
        ((TextView) findViewById(R.id.tv_recipe_name)).setText(recipe.getName());

        StringBuilder sb = new StringBuilder();
        for (Recipe.Ingredient ing : recipe.getIngredients()) {
            sb.append("\u2022 ")
              .append(IngredientMatcher.formatQty(ing.quantity)).append(" ")
              .append(ing.unit).append(" ")
              .append(ing.name).append("\n");
        }
        ((TextView) findViewById(R.id.tv_ingredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tv_steps)).setText(recipe.getSteps());
    }
}
