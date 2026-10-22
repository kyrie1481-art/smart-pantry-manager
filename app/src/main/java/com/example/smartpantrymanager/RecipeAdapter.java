package com.example.smartpantrymanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

/** Custom adapter: shows each recipe as a card. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    // Only used by the "Almost there" list: recipe id -> name of the one missing ingredient
    private Map<Integer, String> missingNotes = null;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    /** Normal (strict) list: every ingredient is available. */
    public void setRecipes(List<Recipe> newRecipes) {
        setRecipes(newRecipes, null);
    }

    /** List with an optional "Missing: ..." note per recipe (used by the Almost there tab). */
    public void setRecipes(List<Recipe> newRecipes, Map<Integer, String> notes) {
        this.recipes = newRecipes;
        this.missingNotes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        final Recipe recipe = recipes.get(position);

        holder.tvName.setText(recipe.getName());

        // Second line: either "you have them all" or which ingredient is missing
        int teal = ContextCompat.getColor(holder.itemView.getContext(), R.color.teal_accent);
        int amber = ContextCompat.getColor(holder.itemView.getContext(), R.color.warning_amber);
        if (missingNotes != null && missingNotes.containsKey(recipe.getId())) {
            holder.tvCount.setText("Missing: " + missingNotes.get(recipe.getId()));
            holder.tvCount.setTextColor(amber);
        } else {
            holder.tvCount.setText(recipe.getIngredients().size() + " ingredients - you have them all");
            holder.tvCount.setTextColor(teal);
        }

        // Short preview of the ingredient names, e.g. "egg, butter, salt"
        StringBuilder preview = new StringBuilder();
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (preview.length() > 0) {
                preview.append(", ");
            }
            preview.append(ingredient.getName());
        }
        holder.tvIngredients.setText(preview.toString());

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCount;
        final TextView tvIngredients;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            tvCount = itemView.findViewById(R.id.tvRecipeCount);
            tvIngredients = itemView.findViewById(R.id.tvRecipeIngredients);
        }
    }
}
