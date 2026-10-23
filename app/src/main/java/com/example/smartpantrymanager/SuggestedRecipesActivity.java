package com.example.smartpantrymanager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Screen 3: Suggested Recipes.
 * "Ready" tab: runs the strict-matching rule (RecipeMatcher) and lists ONLY the recipes
 * the user can cook right now.
 * "Almost there" tab (bonus): recipes missing exactly ONE ingredient, kept clearly
 * separate from the strict list.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper dbHelper;
    private RecipeAdapter adapter;
    private TextView tvEmpty;
    private TextView tvSubtitle;
    private MaterialButton btnReady;
    private MaterialButton btnAlmost;
    private boolean showingAlmost = false; // false = strict "Ready" tab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        NavigationHelper.applyInsets(findViewById(R.id.root));

        dbHelper = new DatabaseHelper(this);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        btnReady = findViewById(R.id.btnReady);
        btnAlmost = findViewById(R.id.btnAlmost);

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<Recipe>(), this);
        recycler.setAdapter(adapter);

        btnReady.setOnClickListener(v -> {
            showingAlmost = false;
            loadSuggestions();
        });
        btnAlmost.setOnClickListener(v -> {
            showingAlmost = true;
            loadSuggestions();
        });

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

        // The strict-matching rule decides which recipes are "Ready"
        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(allRecipes, pantry);
        // Bonus list: exactly one ingredient missing
        List<Recipe> almost = RecipeMatcher.getAlmostThereRecipes(allRecipes, pantry);

        btnReady.setText("Ready (" + suggested.size() + ")");
        btnAlmost.setText("Almost there (" + almost.size() + ")");
        styleTab(btnReady, !showingAlmost);
        styleTab(btnAlmost, showingAlmost);

        List<Recipe> shown;
        if (showingAlmost) {
            Map<Integer, String> notes = new HashMap<>();
            for (Recipe recipe : almost) {
                notes.put(recipe.getId(), RecipeMatcher.getMissingIngredients(recipe, pantry).get(0));
            }
            adapter.setRecipes(almost, notes);
            tvSubtitle.setText("Missing just one ingredient (not ready to cook yet)");
            tvEmpty.setText("No recipes are one ingredient away yet");
            shown = almost;
        } else {
            adapter.setRecipes(suggested, null);
            tvSubtitle.setText(suggested.size() + " of " + allRecipes.size()
                    + " recipes you can cook right now");
            tvEmpty.setText("No recipes match your pantry yet - add more ingredients");
            shown = suggested;
        }

        // Basic feedback instead of a blank screen when nothing matches
        tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /** Teal = selected tab, dark card colour = not selected. */
    private void styleTab(MaterialButton button, boolean selected) {
        int background = ContextCompat.getColor(this, selected ? R.color.teal_accent : R.color.card_surface);
        int text = ContextCompat.getColor(this, selected ? R.color.black_bg : R.color.text_primary);
        button.setBackgroundTintList(ColorStateList.valueOf(background));
        button.setTextColor(text);
    }

    /** Recipe tapped: open the detail screen and pass the recipe id in the Intent. */
    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
