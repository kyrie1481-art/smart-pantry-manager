package com.example.smartpantrymanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the local SQLite database: creates the tables, seeds the recipes on first run,
 * and provides full CRUD (Create, Read, Update, Delete) for pantry items.
 * Every screen talks to the database through this one class.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "pantry.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_PANTRY = "pantry";
    private static final String TABLE_RECIPES = "recipes";
    private static final String TABLE_INGREDIENTS = "recipe_ingredients";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /** Runs only once: the very first time the database file is created. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL, "
                + "expiry TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "recipe_id INTEGER NOT NULL, "
                + "name TEXT NOT NULL, "
                + "quantity REAL NOT NULL, "
                + "unit TEXT NOT NULL)");

        seedRecipes(db); // pre-load the recipe collection
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // =====================================================================
    //  PANTRY CRUD
    // =====================================================================

    /** CREATE: insert a new pantry item and return its new row id. */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry", item.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, values);
    }

    /** READ: all pantry items, sorted A-Z. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, quantity, unit, expiry FROM "
                + TABLE_PANTRY + " ORDER BY name COLLATE NOCASE", null);
        while (cursor.moveToNext()) {
            items.add(readPantryItem(cursor));
        }
        cursor.close();
        return items;
    }

    /** READ: one pantry item by id (used by the edit screen). Returns null if not found. */
    public PantryItem getPantryItem(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, quantity, unit, expiry FROM "
                + TABLE_PANTRY + " WHERE id = ?", new String[]{String.valueOf(id)});
        PantryItem item = null;
        if (cursor.moveToFirst()) {
            item = readPantryItem(cursor);
        }
        cursor.close();
        return item;
    }

    /** UPDATE: overwrite an existing pantry item (matched by its id). */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", item.getName());
        values.put("quantity", item.getQuantity());
        values.put("unit", item.getUnit());
        values.put("expiry", item.getExpiryDate());
        return db.update(TABLE_PANTRY, values, "id = ?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: remove a pantry item by id. */
    public void deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PANTRY, "id = ?", new String[]{String.valueOf(id)});
    }

    /** Turns the current cursor row into a PantryItem object. */
    private PantryItem readPantryItem(Cursor cursor) {
        return new PantryItem(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getDouble(2),
                cursor.getString(3),
                cursor.isNull(4) ? "" : cursor.getString(4));
    }

    // =====================================================================
    //  RECIPES (read only: they are pre-loaded)
    // =====================================================================

    /** READ: every recipe together with its ingredient list. */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, steps FROM " + TABLE_RECIPES
                + " ORDER BY name", null);
        while (cursor.moveToNext()) {
            int recipeId = cursor.getInt(0);
            recipes.add(new Recipe(recipeId, cursor.getString(1), cursor.getString(2),
                    getIngredientsForRecipe(db, recipeId)));
        }
        cursor.close();
        return recipes;
    }

    /** READ: one recipe by id (used by the recipe detail screen). Returns null if not found. */
    public Recipe getRecipeById(int recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name, steps FROM " + TABLE_RECIPES
                + " WHERE id = ?", new String[]{String.valueOf(recipeId)});
        Recipe recipe = null;
        if (cursor.moveToFirst()) {
            recipe = new Recipe(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                    getIngredientsForRecipe(db, recipeId));
        }
        cursor.close();
        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT name, quantity, unit FROM " + TABLE_INGREDIENTS
                + " WHERE recipe_id = ?", new String[]{String.valueOf(recipeId)});
        while (cursor.moveToNext()) {
            list.add(new RecipeIngredient(cursor.getString(0), cursor.getDouble(1),
                    cursor.getString(2)));
        }
        cursor.close();
        return list;
    }

    // =====================================================================
    //  SEED DATA (runs once, on first launch)
    // =====================================================================

    /** Inserts one recipe plus its ingredients. Ingredient format: "name|quantity|unit". */
    private void insertRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put("name", name);
        recipeValues.put("steps", steps);
        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);

        for (String line : ingredients) {
            String[] parts = line.split("\\|");
            ContentValues ingredientValues = new ContentValues();
            ingredientValues.put("recipe_id", recipeId);
            ingredientValues.put("name", parts[0]);
            ingredientValues.put("quantity", Double.parseDouble(parts[1]));
            ingredientValues.put("unit", parts[2]);
            db.insert(TABLE_INGREDIENTS, null, ingredientValues);
        }
    }

    private void seedRecipes(SQLiteDatabase db) {
        insertRecipe(db, "Scrambled Eggs",
                "1. Crack the eggs into a bowl and whisk with salt.\n2. Melt butter in a pan on low heat.\n3. Pour in the eggs and stir gently until just set.",
                "egg|2|pcs", "butter|10|g", "salt|1|tsp");

        insertRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs with salt.\n2. Melt butter in a pan and pour in the eggs.\n3. Add grated cheese, fold in half and cook until golden.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g", "salt|1|tsp");

        insertRecipe(db, "Tomato Pasta",
                "1. Boil the pasta until soft.\n2. Fry the onion and garlic in oil.\n3. Add chopped tomatoes and salt and simmer for 10 minutes.\n4. Mix the sauce with the pasta.",
                "pasta|200|g", "tomato|3|pcs", "onion|1|pcs", "garlic|2|pcs", "oil|2|tbsp", "salt|1|tsp");

        insertRecipe(db, "Egg Fried Rice",
                "1. Fry the chopped onion in oil.\n2. Push it aside and scramble the eggs in the pan.\n3. Add cooked rice and soy sauce and stir-fry for 5 minutes.",
                "rice|1|cup", "egg|2|pcs", "onion|1|pcs", "oil|1|tbsp", "soy sauce|1|tbsp");

        insertRecipe(db, "Cheese Toast",
                "1. Butter the bread.\n2. Top with cheese.\n3. Grill until the cheese melts.",
                "bread|2|pcs", "cheese|40|g", "butter|10|g");

        insertRecipe(db, "Mashed Potatoes",
                "1. Peel the potatoes and boil until soft.\n2. Drain and mash.\n3. Stir in butter, warm milk and salt.",
                "potato|4|pcs", "butter|30|g", "milk|100|ml", "salt|1|tsp");

        insertRecipe(db, "Pancakes",
                "1. Mix flour, sugar, egg and milk into a smooth batter.\n2. Melt butter in a pan.\n3. Pour in small rounds and cook both sides until golden.",
                "flour|200|g", "egg|1|pcs", "milk|250|ml", "sugar|1|tbsp", "butter|20|g");

        insertRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar.\n2. Dip the bread slices.\n3. Fry in a pan until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tsp");

        insertRecipe(db, "Tomato Soup",
                "1. Fry the onion and garlic in butter.\n2. Add chopped tomatoes and salt.\n3. Simmer for 20 minutes, then blend until smooth.",
                "tomato|6|pcs", "onion|1|pcs", "garlic|2|pcs", "butter|20|g", "salt|1|tsp");

        insertRecipe(db, "Garlic Butter Rice",
                "1. Cook the rice.\n2. Melt butter and fry the chopped garlic.\n3. Stir the garlic butter and salt through the rice.",
                "rice|1|cup", "butter|20|g", "garlic|3|pcs", "salt|1|tsp");

        insertRecipe(db, "Cheesy Pasta",
                "1. Boil the pasta.\n2. Warm milk and butter in a pot and melt in the cheese.\n3. Mix the cheese sauce through the pasta.",
                "pasta|200|g", "cheese|100|g", "milk|100|ml", "butter|20|g");

        insertRecipe(db, "Chicken and Rice",
                "1. Fry the onion in oil, then add the chicken pieces and salt.\n2. Add rice and 2 cups of water.\n3. Cover and simmer for 20 minutes.",
                "chicken|300|g", "rice|1|cup", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");

        insertRecipe(db, "Loaded Baked Potatoes",
                "1. Bake whole potatoes at 200 C for 50 minutes.\n2. Cut open and add butter, salt and cheese.\n3. Return to the oven for 5 minutes.",
                "potato|2|pcs", "butter|20|g", "cheese|40|g", "salt|1|tsp");

        insertRecipe(db, "Veggie Stir Fry",
                "1. Slice the carrots, onion and cabbage.\n2. Stir-fry in hot oil for 5 minutes.\n3. Add soy sauce and toss.",
                "carrot|2|pcs", "onion|1|pcs", "cabbage|200|g", "oil|2|tbsp", "soy sauce|1|tbsp");

        insertRecipe(db, "Tuna Pasta",
                "1. Boil the pasta.\n2. Fry the onion in oil, then add the tuna and salt.\n3. Toss with the pasta.",
                "pasta|200|g", "tuna|1|pcs", "onion|1|pcs", "oil|1|tbsp", "salt|1|tsp");

        insertRecipe(db, "Egg Sandwich",
                "1. Boil or fry the eggs and season with salt.\n2. Butter the bread.\n3. Place the eggs between the slices.",
                "bread|2|pcs", "egg|2|pcs", "butter|10|g", "salt|1|tsp");

        insertRecipe(db, "Banana Pancakes",
                "1. Mash the bananas.\n2. Mix with the eggs and flour.\n3. Fry spoonfuls until golden on both sides.",
                "banana|2|pcs", "egg|2|pcs", "flour|50|g");

        insertRecipe(db, "Rice Pudding",
                "1. Put the rice, milk and sugar in a pot.\n2. Simmer on low heat for 30 minutes, stirring often.\n3. Serve warm.",
                "rice|1|cup", "milk|500|ml", "sugar|2|tbsp");

        insertRecipe(db, "Beans on Toast",
                "1. Fry the chopped onion in butter.\n2. Add the beans and heat through.\n3. Serve on toasted bread.",
                "beans|1|pcs", "bread|2|pcs", "onion|1|pcs", "butter|10|g");

        insertRecipe(db, "Chicken Stir Fry",
                "1. Slice the chicken, carrots and onion.\n2. Fry the chicken in hot oil until cooked.\n3. Add the vegetables and soy sauce and cook for 5 minutes.",
                "chicken|300|g", "carrot|2|pcs", "onion|1|pcs", "oil|2|tbsp", "soy sauce|1|tbsp");
    }
}
