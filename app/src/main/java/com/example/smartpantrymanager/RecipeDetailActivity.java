package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

/**
 * Screen 4: Recipe Detail.
 * Receives a recipe id through the Intent, loads that recipe from the database and shows
 * the full ingredient list and the method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Key used to pass the recipe id from the suggestions list to this screen. */
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        NavigationHelper.applyInsets(findViewById(R.id.root));

        MaterialButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Read the id that the previous screen put into the Intent
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipeById(recipeId);

        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        TextView tvIngredients = findViewById(R.id.tvIngredients);
        TextView tvSteps = findViewById(R.id.tvSteps);

        tvTitle.setText(recipe.getName());

        // Build the ingredient list, one line per ingredient: "- 2 pcs egg"
        StringBuilder ingredientText = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (ingredientText.length() > 0) {
                ingredientText.append("\n");
            }
            ingredientText.append("\u2022 ")
                    .append(AppUtils.formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append(" ")
                    .append(ingredient.getName());
        }
        tvIngredients.setText(ingredientText.toString());
        tvSteps.setText(recipe.getSteps());
    }
}
