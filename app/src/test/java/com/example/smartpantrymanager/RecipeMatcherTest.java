package com.example.smartpantrymanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Local unit tests for the strict-matching rule (RecipeMatcher).
 * They run on the computer (no phone needed): right-click this file > Run.
 */
public class RecipeMatcherTest {

    // ----- small helpers to keep the tests readable -----
    private static RecipeIngredient need(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }

    private static PantryItem have(String name, double quantity, String unit) {
        return new PantryItem(1, name, quantity, unit, "");
    }

    private static Recipe recipe(String name, RecipeIngredient... ingredients) {
        return new Recipe(1, name, "steps", new ArrayList<>(Arrays.asList(ingredients)));
    }

    private static List<PantryItem> pantry(PantryItem... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    private static Recipe cheeseToast() {
        return recipe("Cheese Toast", need("bread", 2, "pcs"), need("cheese", 40, "g"), need("butter", 10, "g"));
    }

    // ----- the strict rule -----

    @Test
    public void allIngredientsPresent_recipeIsSuggested() {
        List<PantryItem> p = pantry(have("bread", 4, "pcs"), have("cheese", 200, "g"), have("butter", 250, "g"));
        assertTrue(RecipeMatcher.canMake(cheeseToast(), p));
    }

    @Test
    public void oneIngredientMissing_recipeIsNotSuggested() {
        // butter is missing: 2 of 3 ingredients is NOT enough
        List<PantryItem> p = pantry(have("bread", 4, "pcs"), have("cheese", 200, "g"));
        assertFalse(RecipeMatcher.canMake(cheeseToast(), p));
    }

    @Test
    public void quantityTooSmall_recipeIsNotSuggested() {
        // the recipe needs 2 bread but the pantry has only 1
        List<PantryItem> p = pantry(have("bread", 1, "pcs"), have("cheese", 200, "g"), have("butter", 250, "g"));
        assertFalse(RecipeMatcher.canMake(cheeseToast(), p));
    }

    @Test
    public void emptyPantry_nothingIsSuggested() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(cheeseToast());
        assertEquals(0, RecipeMatcher.getSuggestedRecipes(recipes, pantry()).size());
    }

    @Test
    public void suggestedList_containsOnlyCookableRecipes() {
        Recipe toast = cheeseToast();
        Recipe soup = recipe("Tomato Soup", need("tomato", 6, "pcs"));
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(toast);
        recipes.add(soup);

        List<Recipe> result = RecipeMatcher.getSuggestedRecipes(recipes, pantry(have("tomato", 10, "pcs")));

        assertEquals(1, result.size());
        assertEquals("Tomato Soup", result.get(0).getName());
    }

    // ----- real-world messiness -----

    @Test
    public void pluralAndSingularNamesMatch() {
        Recipe r = recipe("Tomato Pasta", need("tomato", 3, "pcs"));
        assertTrue(RecipeMatcher.canMake(r, pantry(have("Tomatoes", 6, "pcs"))));
    }

    @Test
    public void capitalLettersAndSpacesAreIgnored() {
        Recipe r = recipe("Eggs", need("egg", 2, "pcs"));
        assertTrue(RecipeMatcher.canMake(r, pantry(have("  EGG ", 2, "pcs"))));
    }

    @Test
    public void kilogramsCoverGrams() {
        Recipe r = recipe("Cheesy", need("cheese", 40, "g"));
        assertTrue(RecipeMatcher.canMake(r, pantry(have("cheese", 0.2, "kg"))));
    }

    @Test
    public void litresCoverMillilitres() {
        Recipe r = recipe("Milky", need("milk", 100, "ml"));
        assertTrue(RecipeMatcher.canMake(r, pantry(have("milk", 1, "l"))));
    }

    @Test
    public void convertedQuantityStillTooSmall_isRejected() {
        Recipe r = recipe("Big cheese", need("cheese", 500, "g"));
        assertFalse(RecipeMatcher.canMake(r, pantry(have("cheese", 0.2, "kg"))));
    }

    @Test
    public void duplicateEntriesAreAddedUp() {
        Recipe r = recipe("Eggs", need("egg", 2, "pcs"));
        assertTrue(RecipeMatcher.canMake(r, pantry(have("egg", 1, "pcs"), have("egg", 1, "pcs"))));
    }

    @Test
    public void unitsThatCannotBeConverted_areAcceptedWhenUserOwnsSome() {
        // recipe says 1 teaspoon of salt, pantry says 500 g of salt
        Recipe r = recipe("Salty", need("salt", 1, "tsp"));
        assertTrue(RecipeMatcher.canMake(r, pantry(have("salt", 500, "g"))));
    }

    @Test
    public void zeroQuantityDoesNotCount() {
        Recipe r = recipe("Rice", need("rice", 1, "cup"));
        assertFalse(RecipeMatcher.canMake(r, pantry(have("rice", 0, "cup"))));
    }

    // ----- name cleaning -----

    @Test
    public void normalizeName_makesNamesLowerCaseAndSingular() {
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
        assertEquals("potato", RecipeMatcher.normalizeName("Potatoes"));
        assertEquals("berry", RecipeMatcher.normalizeName("Berries"));
        assertEquals("egg", RecipeMatcher.normalizeName("  EGGS  "));
        assertEquals("soy sauce", RecipeMatcher.normalizeName("Soy Sauces"));
    }

    @Test
    public void normalizeName_leavesNonPluralsAlone() {
        assertEquals("cheese", RecipeMatcher.normalizeName("Cheese"));
        assertEquals("hummus", RecipeMatcher.normalizeName("Hummus"));
        assertEquals("", RecipeMatcher.normalizeName(null));
    }
}
