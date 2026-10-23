# Smart Pantry Manager

A Java Android app that cuts food waste by suggesting recipes **strictly** from the ingredients
you already have at home - no shopping trip required.

## Features
- Pantry management: add, edit and delete ingredients (name, quantity, unit, optional expiry date) with input validation
- 20 pre-loaded recipes (seeded on first run)
- **Strict matching**: a recipe is only suggested if *every* ingredient is in your pantry in at least the required quantity
- Robust matching: ignores case, handles singular/plural ("tomato" = "tomatoes") and converts units (1 kg covers 250 g)
- **Almost there (bonus)**: a separate tab listing recipes that are missing exactly one ingredient, never mixed into the strict list
- Recipe detail screen with the full ingredient list and method
- Expiring-soon alerts and a default-unit preference (Settings)
- Dark indigo / black / teal theme

## Database choice: SQLite
I chose **SQLite** (using `SQLiteOpenHelper`) because it is built into Android, stores data locally
on the device, works fully offline, needs no server or account, and it is the approach covered in
the module. Data is stored in three tables: `pantry`, `recipes` and `recipe_ingredients`, and it
persists after the app is closed and reopened.

## Tech
- Java (not Kotlin), Android Studio, minimum SDK 24
- RecyclerView + custom adapters, Intents, BottomNavigationView, SharedPreferences, Material Components
- JUnit 4 unit tests for the matching logic
- No Google Maps, mapping SDK or location services

## Setup / run instructions
1. Install Android Studio and clone the repository:
   `git clone https://github.com/kyrie1481-art/smart-pantry-manager.git`
2. Open the project folder in Android Studio and wait for Gradle to sync.
3. Connect an Android phone (USB debugging on) or start an emulator (API 24+).
4. Press the green **Run** button.

## Running the unit tests
In Android Studio, open `app/src/test/java/.../RecipeMatcherTest.java`, right-click the file name and
choose **Run 'RecipeMatcherTest'**. The tests check the strict rule (4 of 5 ingredients = not suggested),
plural names, unit conversion, duplicate entries and the Almost There list.

## Screens
1. Pantry List (home) 2. Add / Edit Ingredient 3. Suggested Recipes (Ready / Almost there tabs)
4. Recipe Detail 5. Settings
