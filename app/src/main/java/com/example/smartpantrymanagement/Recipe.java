package com.example.smartpantrymanagement;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for a recipe. Each recipe holds the list of ingredients
 * it requires; the strict-matching logic compares these against the pantry.
 */
public class Recipe {
    private long id;
    private String name;
    private String steps;
    private List<Ingredient> ingredients = new ArrayList<>();

    public Recipe(long id, String name, String steps) {
        this.id = id;
        this.name = name;
        this.steps = steps;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getSteps() { return steps; }
    public List<Ingredient> getIngredients() { return ingredients; }

    /** One required ingredient of a recipe (name, amount, unit). */
    public static class Ingredient {
        public final String name;
        public final double quantity;
        public final String unit;

        public Ingredient(String name, double quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
