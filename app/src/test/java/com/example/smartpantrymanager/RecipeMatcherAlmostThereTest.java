package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Local unit tests for the bonus "Almost There" list. */
public class RecipeMatcherAlmostThereTest {

    private static RecipeIngredient need(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static PantryItem have(String name, double quantity, String unit) {
        return new PantryItem(1, name, quantity, unit, "");
    }

    private static List<PantryItem> pantry(PantryItem... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    private static Recipe cheeseToast() {
        List<RecipeIngredient> list = new ArrayList<>();
        list.add(need("bread", 2, "pcs"));
        list.add(need("cheese", 40, "g"));
        list.add(need("butter", 10, "g"));
        return new Recipe(1, "Cheese Toast", "steps", list);
    }

    private static List<Recipe> recipes() {
        List<Recipe> list = new ArrayList<>();
        list.add(cheeseToast());
        return list;
    }

    @Test
    public void missingExactlyOneIngredient_isAlmostThere() {
        List<PantryItem> p = pantry(have("bread", 4, "pcs"), have("cheese", 200, "g"));
        List<Recipe> almost = RecipeMatcher.getAlmostThereRecipes(recipes(), p);
        assertEquals(1, almost.size());
        assertEquals("butter", RecipeMatcher.getMissingIngredients(almost.get(0), p).get(0));
    }

    @Test
    public void missingTwoIngredients_isNotAlmostThere() {
        List<PantryItem> p = pantry(have("bread", 4, "pcs"));
        assertEquals(0, RecipeMatcher.getAlmostThereRecipes(recipes(), p).size());
    }

    @Test
    public void readyRecipe_isNotInAlmostThereList() {
        List<PantryItem> p = pantry(have("bread", 4, "pcs"), have("cheese", 200, "g"), have("butter", 250, "g"));
        assertEquals(0, RecipeMatcher.getAlmostThereRecipes(recipes(), p).size());
        assertEquals(1, RecipeMatcher.getSuggestedRecipes(recipes(), p).size());
    }

    @Test
    public void almostThereRecipe_neverAppearsInStrictList() {
        List<PantryItem> p = pantry(have("bread", 4, "pcs"), have("cheese", 200, "g"));
        assertEquals(0, RecipeMatcher.getSuggestedRecipes(recipes(), p).size());
        assertEquals(1, RecipeMatcher.getAlmostThereRecipes(recipes(), p).size());
    }

    @Test
    public void missingIngredients_listsEveryMissingOne() {
        List<PantryItem> p = pantry(have("bread", 4, "pcs"));
        List<String> missing = RecipeMatcher.getMissingIngredients(cheeseToast(), p);
        assertEquals(2, missing.size());
        assertTrue(missing.contains("cheese"));
        assertTrue(missing.contains("butter"));
    }
}
