package com.example.smartpantrymanagement;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Core business logic: the STRICT-MATCHING RULE.
 * A recipe is suggested only if EVERY ingredient is in the pantry in at
 * least the required quantity. Names and units are normalised first so
 * that "Tomatoes" matches "tomato" and 1 kg covers 500 g.
 */
public class IngredientMatcher {

    private static final int TYPE_MASS = 0, TYPE_VOLUME = 1, TYPE_COUNT = 2, TYPE_OTHER = 3;

    /** Lower-cases, trims and removes simple plural endings. */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
        if (s.length() > 3 && s.endsWith("ies")) return s.substring(0, s.length() - 3) + "y"; // berries -> berry
        if (s.endsWith("oes")) return s.substring(0, s.length() - 2);                          // tomatoes -> tomato
        if (s.endsWith("ches") || s.endsWith("shes") || s.endsWith("sses") || s.endsWith("xes"))
            return s.substring(0, s.length() - 2);                                             // peaches -> peach
        if (s.length() > 3 && s.endsWith("s") && !s.endsWith("ss"))
            return s.substring(0, s.length() - 1);                                             // eggs -> egg
        return s;
    }

    /** Maps unit spellings to a standard short form. */
    public static String normalizeUnit(String raw) {
        if (raw == null) return "";
        String u = raw.toLowerCase(Locale.ROOT).trim();
        switch (u) {
            case "gram": case "grams": return "g";
            case "kilogram": case "kilograms": case "kgs": return "kg";
            case "milliliter": case "milliliters": case "millilitre": case "millilitres": return "ml";
            case "liter": case "liters": case "litre": case "litres": return "l";
            case "teaspoon": case "teaspoons": return "tsp";
            case "tablespoon": case "tablespoons": return "tbsp";
            case "cups": return "cup";
            case "piece": case "pieces": case "pc": case "each": case "unit": case "units": return "pcs";
            default: return u;
        }
    }

    private static int unitType(String unit) {
        switch (normalizeUnit(unit)) {
            case "g": case "kg": return TYPE_MASS;
            case "ml": case "l": case "tsp": case "tbsp": case "cup": return TYPE_VOLUME;
            case "pcs": return TYPE_COUNT;
            default: return TYPE_OTHER;
        }
    }

    /** Converts a quantity to its base unit (g, ml or pcs). */
    private static double toBase(double qty, String unit) {
        switch (normalizeUnit(unit)) {
            case "kg": return qty * 1000;
            case "l": return qty * 1000;
            case "tsp": return qty * 5;
            case "tbsp": return qty * 15;
            case "cup": return qty * 240;
            default: return qty; // g, ml, pcs
        }
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + unitType(unit);
    }

    /** Adds up pantry quantities per (normalised name, unit type) in base units. */
    public static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String k = key(item.getName(), item.getUnit());
            double amount = toBase(item.getQuantity(), item.getUnit());
            Double existing = totals.get(k);
            totals.put(k, existing == null ? amount : existing + amount);
        }
        return totals;
    }

    /** True only if EVERY ingredient is available in the needed quantity. */
    public static boolean canMake(Recipe recipe, Map<String, Double> pantryTotals) {
        if (recipe.getIngredients().isEmpty()) return false;
        for (Recipe.Ingredient ing : recipe.getIngredients()) {
            Double have = pantryTotals.get(key(ing.name, ing.unit));
            double need = toBase(ing.quantity, ing.unit);
            if (have == null || have + 1e-9 < need) {
                return false; // one missing/insufficient ingredient rules the recipe out
            }
        }
        return true;
    }

    /** Returns only the recipes the user can make right now. */
    public static List<Recipe> getSuggestedRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        Map<String, Double> totals = buildPantryTotals(pantry);
        List<Recipe> result = new ArrayList<>();
        for (Recipe r : recipes) {
            if (canMake(r, totals)) result.add(r);
        }
        return result;
    }

    /** Shows 2.0 as "2" and 0.5 as "0.5". */
    public static String formatQty(double qty) {
        return new DecimalFormat("0.##").format(qty);
    }
}
