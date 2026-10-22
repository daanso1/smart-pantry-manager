Smart Pantry Manager

**Module: Mobile App Develelopment
**Author: Daanish Kamroodeen

Description

Smart Pantry Manager is a Java Android app that helps reduce food waste. The user records the ingredients they already have at home, and the app suggests recipes they can cook **using strictly those ingredients**.

Its core feature is the **strict-matching rule**: a recipe is only suggested if *every* ingredient it needs is in the pantry in at least the required quantity. If a recipe needs five ingredients and the pantry holds four, it is not shown.

Features

- Add, edit and delete pantry items (name, quantity, unit, optional expiry date)
- Pantry list using a RecyclerView with a custom adapter
- 31 pre-loaded recipes, seeded into the database on first run
- Suggested Recipes screen that lists only recipes the user can make right now
- Recipe detail screen with ingredients and method
- Settings screen with an expiring-soon alert toggle
- Message shown when no recipes match the pantry
- Input validation and bottom navigation

The matching copes with plurals ("tomatoes" = "tomato"), unit differences (0.1 kg = 100 g) and duplicate entries of the same ingredient.

Database: SQLite

I chose **SQLite** (`SQLiteOpenHelper`) because it is stored locally on the device, works offline without a server or account, suits the app's small structured data, and is the approach covered in the module. Data persists after the app is closed and reopened.

Tables: `pantry` (id, name, quantity, unit, expiry), `recipes` (id, name, steps) and `recipe_ingredients` (id, recipe_id, name, quantity, unit).

## Setup / How to run

Requires Android Studio and either an emulator or an Android phone.

1. Clone the repository:
```
   git clone https://github.com/daanso1/smart-pantry-manager.git
```
2. Open the folder in Android Studio and wait for the Gradle sync to finish.
3. Create an emulator in Device Manager (e.g. Pixel, API 34) or connect a phone with USB debugging on.
4. Press **Run ▶**.

No API keys or configuration are needed. The database and recipes are created on first launch.

*The app does not use Google Maps or any location services.*