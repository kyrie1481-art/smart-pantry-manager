package com.example.smartpantrymanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * THE CORE LOGIC OF THE APP: the strict-matching rule.
 *
 * A recipe is suggested ONLY IF every single ingredient it needs is in the pantry
 * in at least the required quantity. 4 out of 5 ingredients = NOT suggested.
 *
 * To cope with real-world messiness it also:
 *  - ignores upper/lower case and extra spaces        ("Tomato " = "tomato")
 *  - treats plural and singular as the same word      ("tomatoes" = "tomato")
 *  - converts units before comparing                  (1 kg covers 250 g)
 *  - adds up the same ingredient entered more than once
 */
public class RecipeMatcher {

    // Unit "families": only units in the same family can be converted into each other
    private static final int CAT_UNKNOWN = 0;
    private static final int CAT_MASS = 1;    // g, kg
    private static final int CAT_VOLUME = 2;  // ml, l, cup, tbsp, tsp
    private static final int CAT_COUNT = 3;   // pcs

    /** Returns only the recipes the user can cook right now. */
    public static List<Recipe> getSuggestedRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (canMake(recipe, pantry)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    /** True only if EVERY ingredient of the recipe is available in the pantry. */
    public static boolean canMake(Recipe recipe, List<PantryItem> pantry) {
        if (recipe.getIngredients().isEmpty()) {
            return false; // a recipe with no ingredients is not a real recipe
        }
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasEnough(needed, pantry)) {
                return false; // one missing ingredient is enough to reject the recipe
            }
        }
        return true;
    }

    /**
     * BONUS "Almost There" list: recipes that are missing EXACTLY ONE ingredient.
     * These are never mixed into the strict suggestions; the screen shows them separately.
     */
    public static List<Recipe> getAlmostThereRecipes(List<Recipe> recipes, List<PantryItem> pantry) {
        List<Recipe> almost = new ArrayList<>();
        for (Recipe recipe : recipes) {
            if (getMissingIngredients(recipe, pantry).size() == 1) {
                almost.add(recipe);
            }
        }
        return almost;
    }

    /** The names of the ingredients of this recipe that the pantry cannot cover. */
    public static List<String> getMissingIngredients(Recipe recipe, List<PantryItem> pantry) {
        List<String> missing = new ArrayList<>();
        for (RecipeIngredient needed : recipe.getIngredients()) {
            if (!hasEnough(needed, pantry)) {
                missing.add(needed.getName());
            }
        }
        return missing;
    }

    /** Checks one recipe ingredient against the whole pantry. */
    private static boolean hasEnough(RecipeIngredient needed, List<PantryItem> pantry) {
        String neededName = normalizeName(needed.getName());
        int neededCategory = unitCategory(needed.getUnit());
        double neededBase = toBaseQuantity(needed.getQuantity(), needed.getUnit());

        double totalBase = 0;                  // how much of it the pantry holds (in base units)
        boolean incompatibleUnitFound = false; // e.g. recipe says "1 tsp salt", pantry says "500 g salt"

        for (PantryItem item : pantry) {
            if (item.getQuantity() <= 0) {
                continue; // an empty item does not count
            }
            if (!normalizeName(item.getName()).equals(neededName)) {
                continue; // different ingredient
            }
            if (unitCategory(item.getUnit()) == neededCategory) {
                totalBase += toBaseQuantity(item.getQuantity(), item.getUnit());
            } else {
                incompatibleUnitFound = true;
            }
        }

        // Enough in compatible units?
        if (totalBase + 0.000001 >= neededBase) {
            return true;
        }
        // Units cannot be compared (weight vs spoons): accept the ingredient if the user owns some
        return totalBase == 0 && incompatibleUnitFound;
    }

    /** Lower-cases, removes symbols and turns every word into its singular form. */
    public static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z\\s]", " ").trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        String[] words = cleaned.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(singularize(word));
        }
        return result.toString();
    }

    /** Simple English plural -> singular rules (no full NLP needed). */
    private static String singularize(String word) {
        if (word.length() <= 3) {
            return word;
        }
        if (word.equals("leaves")) return "leaf";
        if (word.equals("loaves")) return "loaf";

        if (word.endsWith("ies") && word.length() > 4) {
            return word.substring(0, word.length() - 3) + "y";      // berries -> berry
        }
        if (word.endsWith("sses") || word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("xes") || word.endsWith("oes")) {
            return word.substring(0, word.length() - 2);            // tomatoes -> tomato
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                                            // hummus, swiss stay the same
        }
        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);            // eggs -> egg
        }
        return word;
    }

    /** Which unit family does this unit belong to? */
    private static int unitCategory(String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "g":
            case "kg":
                return CAT_MASS;
            case "ml":
            case "l":
            case "cup":
            case "tbsp":
            case "tsp":
                return CAT_VOLUME;
            case "pcs":
                return CAT_COUNT;
            default:
                return CAT_UNKNOWN;
        }
    }

    /** Converts a quantity to the smallest unit of its family (g, ml or pieces). */
    private static double toBaseQuantity(double quantity, String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        switch (u) {
            case "kg":
            case "l":
                return quantity * 1000;
            case "cup":
                return quantity * 240;
            case "tbsp":
                return quantity * 15;
            case "tsp":
                return quantity * 5;
            default:
                return quantity; // g, ml, pcs
        }
    }
}
