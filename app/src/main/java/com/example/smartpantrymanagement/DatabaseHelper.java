package com.example.smartpantrymanagement;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all SQLite work for the app: table creation, seeding the recipe
 * collection on first run, and CRUD operations for pantry items.
 *
 * Tables:
 *   pantry(id, name, quantity, unit, expiry)
 *   recipes(id, name, steps)
 *   recipe_ingredients(id, recipe_id, name, quantity, unit)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called once, the first time the database is created (first app run).
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, quantity REAL NOT NULL, unit TEXT NOT NULL, expiry TEXT)");
        db.execSQL("CREATE TABLE recipes (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, steps TEXT NOT NULL)");
        db.execSQL("CREATE TABLE recipe_ingredients (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, name TEXT NOT NULL, quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, FOREIGN KEY(recipe_id) REFERENCES recipes(id))");
        seedRecipes(db);
        seedMoreRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 2 added more recipes. Insert them WITHOUT touching the
        // user's pantry data, so nothing the user saved is lost.
        if (oldVersion < 2) {
            seedMoreRecipes(db);
        }
    }

    // ------------------------------------------------------------------
    // PANTRY CRUD
    // ------------------------------------------------------------------

    /** CREATE: insert a new pantry item, returns its new row id (or -1). */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert("pantry", null, toValues(item));
    }

    /** READ: all pantry items, sorted alphabetically. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("pantry", null, null, null, null, null, "name COLLATE NOCASE ASC");
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();
        return list;
    }

    /** READ: a single pantry item by id (null if not found). */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("pantry", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        PantryItem item = c.moveToFirst() ? fromCursor(c) : null;
        c.close();
        return item;
    }

    /** UPDATE: overwrite an existing item; returns number of rows changed. */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update("pantry", toValues(item), "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: remove an item; returns number of rows deleted. */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("pantry", "id = ?", new String[]{String.valueOf(id)});
    }

    private ContentValues toValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry", item.getExpiryDate());
        return v;
    }

    private PantryItem fromCursor(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry")));
    }

    // ------------------------------------------------------------------
    // RECIPE READS
    // ------------------------------------------------------------------

    /** Returns every recipe with its ingredient list filled in. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("recipes", null, null, null, null, null, "name ASC");
        while (c.moveToNext()) {
            Recipe r = new Recipe(c.getLong(0), c.getString(1), c.getString(2));
            loadIngredients(db, r);
            recipes.add(r);
        }
        c.close();
        return recipes;
    }

    /** Returns one recipe by id (null if not found). */
    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("recipes", null, "id = ?", new String[]{String.valueOf(id)},
                null, null, null);
        Recipe r = null;
        if (c.moveToFirst()) {
            r = new Recipe(c.getLong(0), c.getString(1), c.getString(2));
            loadIngredients(db, r);
        }
        c.close();
        return r;
    }

    private void loadIngredients(SQLiteDatabase db, Recipe r) {
        Cursor c = db.query("recipe_ingredients", null, "recipe_id = ?",
                new String[]{String.valueOf(r.getId())}, null, null, null);
        while (c.moveToNext()) {
            r.getIngredients().add(new Recipe.Ingredient(
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getDouble(c.getColumnIndexOrThrow("quantity")),
                    c.getString(c.getColumnIndexOrThrow("unit"))));
        }
        c.close();
    }

    // ------------------------------------------------------------------
    // SEED DATA (runs once, on first launch)
    // ------------------------------------------------------------------

    /** Ingredient format: "name|quantity|unit". */
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put("name", name);
        rv.put("steps", steps);
        long recipeId = db.insert("recipes", null, rv);
        for (String s : ingredients) {
            String[] p = s.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put("recipe_id", recipeId);
            iv.put("name", p[0]);
            iv.put("quantity", Double.parseDouble(p[1]));
            iv.put("unit", p[2]);
            db.insert("recipe_ingredients", null, iv);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs on Toast",
                "1. Beat the eggs with salt.\n2. Melt butter in a pan on low heat.\n3. Stir the eggs gently until just set.\n4. Toast the bread and serve the eggs on top.",
                "egg|3|pcs", "bread|2|pcs", "butter|10|g", "salt|1|g");

        addRecipe(db, "Tomato Pasta",
                "1. Boil pasta until tender, then drain.\n2. Fry chopped onion and garlic in oil.\n3. Add chopped tomatoes and simmer for 10 minutes.\n4. Toss the pasta through the sauce.",
                "pasta|200|g", "tomato|4|pcs", "onion|1|pcs", "garlic|2|pcs", "olive oil|20|ml");

        addRecipe(db, "Cheese Omelette",
                "1. Beat the eggs.\n2. Pour into a hot buttered pan.\n3. Sprinkle cheese on top and fold when set.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");

        addRecipe(db, "Fried Rice",
                "1. Cook rice and let it cool.\n2. Scramble the eggs in a hot pan and set aside.\n3. Fry onion and peas, add the rice.\n4. Stir in the eggs and soy sauce.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "peas|80|g", "soy sauce|20|ml");

        addRecipe(db, "Pancakes",
                "1. Whisk flour, milk and egg into a smooth batter.\n2. Heat butter in a pan.\n3. Pour in batter and cook until golden on both sides.",
                "flour|150|g", "milk|250|ml", "egg|1|pcs", "butter|15|g");

        addRecipe(db, "Cheese Toastie",
                "1. Butter the bread.\n2. Place cheese between the slices.\n3. Grill in a pan until golden and melted.",
                "bread|2|pcs", "cheese|40|g", "butter|10|g");

        addRecipe(db, "Mashed Potatoes",
                "1. Peel and boil potatoes until soft.\n2. Drain and mash with butter and milk.\n3. Season with salt.",
                "potato|500|g", "butter|30|g", "milk|60|ml", "salt|2|g");

        addRecipe(db, "Garlic Butter Pasta",
                "1. Boil pasta and drain.\n2. Melt butter and fry crushed garlic gently.\n3. Toss pasta through the garlic butter.",
                "pasta|200|g", "butter|40|g", "garlic|3|pcs", "salt|1|g");

        addRecipe(db, "Vegetable Omelette",
                "1. Beat the eggs.\n2. Fry chopped onion and tomato in oil.\n3. Pour in the eggs and cook until set.",
                "egg|3|pcs", "onion|1|pcs", "tomato|1|pcs", "olive oil|10|ml");

        addRecipe(db, "Rice and Peas",
                "1. Cook the rice.\n2. Steam the peas.\n3. Mix with butter and salt.",
                "rice|200|g", "peas|100|g", "butter|15|g", "salt|1|g");

        addRecipe(db, "Banana Pancakes",
                "1. Mash the banana.\n2. Mix with the egg and flour.\n3. Fry spoonfuls in butter until golden.",
                "banana|2|pcs", "egg|1|pcs", "flour|60|g", "butter|10|g");

        addRecipe(db, "Tomato Soup",
                "1. Fry onion and garlic in butter.\n2. Add chopped tomatoes and water, simmer 20 minutes.\n3. Blend and season with salt.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "butter|20|g", "salt|2|g");

        addRecipe(db, "Cheesy Mashed Potato Bake",
                "1. Boil and mash the potatoes with butter.\n2. Top with cheese.\n3. Bake at 200C for 15 minutes.",
                "potato|600|g", "cheese|80|g", "butter|30|g");

        addRecipe(db, "French Toast",
                "1. Whisk eggs with milk.\n2. Dip the bread slices.\n3. Fry in butter until golden.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "butter|15|g");

        addRecipe(db, "Egg Fried Potatoes",
                "1. Dice and parboil the potatoes.\n2. Fry in oil with onion until crispy.\n3. Crack in the eggs and cook through.",
                "potato|400|g", "egg|2|pcs", "onion|1|pcs", "olive oil|20|ml");

        addRecipe(db, "Cheese and Tomato Pasta Bake",
                "1. Boil the pasta.\n2. Mix with chopped tomatoes and garlic.\n3. Top with cheese and bake until bubbling.",
                "pasta|250|g", "tomato|4|pcs", "garlic|2|pcs", "cheese|100|g");

        addRecipe(db, "Simple Rice Pudding",
                "1. Simmer rice in milk, stirring often.\n2. Cook for 25 minutes until creamy.\n3. Stir in butter.",
                "rice|100|g", "milk|500|ml", "butter|10|g");
    }

    /** Extra recipes (meat, rice, beans, vegetables) added in database version 2. */
    private void seedMoreRecipes(SQLiteDatabase db) {
        addRecipe(db, "Chicken Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry diced chicken in oil until cooked through.\n3. Add onion and peas, then the rice.\n4. Stir in beaten egg and soy sauce until cooked.",
                "chicken breast|200|g", "rice|200|g", "egg|2|pcs", "peas|80|g",
                "soy sauce|20|ml", "onion|1|pcs", "olive oil|15|ml");

        addRecipe(db, "Chicken and Rice Pot",
                "1. Brown the chicken pieces in oil.\n2. Add chopped onion and carrots.\n3. Stir in rice, salt and 500 ml water.\n4. Cover and simmer for 20 minutes until the rice is tender.",
                "chicken breast|300|g", "rice|200|g", "onion|1|pcs", "carrot|2|pcs",
                "salt|2|g", "olive oil|15|ml");

        addRecipe(db, "Beef Bolognese",
                "1. Fry onion and garlic in oil.\n2. Add the beef mince and brown it.\n3. Add chopped tomatoes and simmer for 20 minutes.\n4. Serve over boiled pasta.",
                "beef mince|300|g", "pasta|200|g", "tomato|4|pcs", "onion|1|pcs",
                "garlic|2|pcs", "olive oil|15|ml");

        addRecipe(db, "Beans on Toast",
                "1. Warm the baked beans in a pan.\n2. Toast and butter the bread.\n3. Spoon the beans over the toast.",
                "baked beans|200|g", "bread|2|pcs", "butter|10|g");

        addRecipe(db, "Chilli Con Carne",
                "1. Fry onion and garlic, then brown the mince.\n2. Add chilli powder, tomatoes and kidney beans.\n3. Simmer for 30 minutes.\n4. Serve with boiled rice.",
                "beef mince|300|g", "kidney beans|200|g", "tomato|3|pcs", "onion|1|pcs",
                "garlic|2|pcs", "chilli powder|3|g", "rice|200|g");

        addRecipe(db, "Bean and Rice Bowl",
                "1. Cook the rice.\n2. Fry onion and tomato in oil.\n3. Add the kidney beans and salt and heat through.\n4. Serve over the rice.",
                "kidney beans|200|g", "rice|200|g", "onion|1|pcs", "tomato|2|pcs",
                "salt|2|g", "olive oil|10|ml");

        addRecipe(db, "Vegetable Stir-Fry",
                "1. Slice the carrots, cabbage and onion thinly.\n2. Fry garlic in hot oil.\n3. Add the vegetables and stir-fry for 5 minutes.\n4. Finish with soy sauce.",
                "carrot|2|pcs", "cabbage|200|g", "onion|1|pcs", "soy sauce|20|ml",
                "olive oil|15|ml", "garlic|2|pcs");

        addRecipe(db, "Lentil Soup",
                "1. Fry onion and garlic in oil.\n2. Add diced carrots and potato, then the lentils.\n3. Cover with water and simmer for 30 minutes.\n4. Season with salt.",
                "lentils|150|g", "carrot|2|pcs", "onion|1|pcs", "potato|200|g",
                "garlic|2|pcs", "salt|2|g", "olive oil|15|ml");

        addRecipe(db, "Chicken and Veg Stir-Fry",
                "1. Slice the chicken, pepper, carrots and onion.\n2. Fry the chicken in oil until cooked.\n3. Add the vegetables and cook for 5 minutes.\n4. Stir in soy sauce.",
                "chicken breast|250|g", "carrot|2|pcs", "bell pepper|1|pcs", "onion|1|pcs",
                "soy sauce|20|ml", "olive oil|15|ml");

        addRecipe(db, "Sausage and Mash",
                "1. Boil and mash the potatoes with butter and milk.\n2. Fry or grill the sausages until browned.\n3. Fry sliced onion until soft.\n4. Serve together.",
                "sausage|4|pcs", "potato|600|g", "butter|30|g", "milk|60|ml", "onion|1|pcs");

        addRecipe(db, "Spinach Omelette",
                "1. Wilt the spinach in butter.\n2. Pour in the beaten eggs.\n3. Add cheese and fold when set.",
                "egg|3|pcs", "spinach|50|g", "cheese|30|g", "butter|10|g");

        addRecipe(db, "Chickpea Curry",
                "1. Fry onion and garlic in oil.\n2. Add curry powder and chopped tomatoes.\n3. Stir in the chickpeas and simmer for 15 minutes.\n4. Serve with boiled rice.",
                "chickpeas|240|g", "tomato|3|pcs", "onion|1|pcs", "garlic|2|pcs",
                "curry powder|5|g", "rice|200|g", "olive oil|15|ml");

        addRecipe(db, "Beef and Potato Stew",
                "1. Brown the beef pieces in a pot.\n2. Add chopped onion, carrots and potatoes.\n3. Cover with water, season with salt.\n4. Simmer gently for 1 hour.",
                "beef|300|g", "potato|400|g", "carrot|2|pcs", "onion|1|pcs", "salt|2|g");

        addRecipe(db, "Vegetable Rice",
                "1. Cook the rice.\n2. Fry onion, diced carrot and pepper in butter.\n3. Add the peas and cook for 3 minutes.\n4. Mix everything with the rice.",
                "rice|200|g", "carrot|2|pcs", "peas|80|g", "onion|1|pcs",
                "bell pepper|1|pcs", "butter|15|g");
    }
}