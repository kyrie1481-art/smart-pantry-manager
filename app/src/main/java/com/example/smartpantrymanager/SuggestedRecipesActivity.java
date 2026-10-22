package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 3: Suggested Recipes.
 * Runs the strict-matching rule (RecipeMatcher) against the current pantry and lists
 * ONLY the recipes the user can cook right now.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;
    private TextView tvEmpty;
    private TextView tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        NavigationHelper.applyInsets(findViewById(R.id.root));

        dbHelper = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<Recipe>(), this);
        recycler.setAdapter(adapter);

        NavigationHelper.setupBottomNav(this, R.id.nav_suggested);
    }

    /** Re-runs the matching every time the screen is shown, so it always reflects the pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = dbHelper.getAllPantryItems();
        List<Recipe> allRecipes = dbHelper.getAllRecipes();

        // The strict-matching rule decides which recipes make it onto this list
        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(allRecipes, pantry);

        adapter.setRecipes(suggested);
        tvSubtitle.setText(suggested.size() + " of " + allRecipes.size()
                + " recipes you can cook right now");

        // Basic feedback instead of a blank screen when nothing matches
        tvEmpty.setVisibility(suggested.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Recipe tapped: open the detail screen and pass the recipe id in the Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
