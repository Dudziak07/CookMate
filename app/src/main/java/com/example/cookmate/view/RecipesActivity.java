package com.example.cookmate.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView; // Dodaj import SearchView
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class RecipesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipesAdapter adapter;
    private List<Recipe> recipes;
    private boolean isFabOpen = false; // Śledzi, czy FAB jest otwarty

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        // Znajdź elementy FAB
        FloatingActionButton fabMain = findViewById(R.id.fab_main);
        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);

        // Listener głównego przycisku hamburgera
        fabMain.setOnClickListener(v -> {
            if (isFabOpen) {
                // Zamknij FAB i zmień ikonę na burger_menu
                fabAddRecipe.setVisibility(View.GONE);
                fabMain.setImageResource(R.drawable.burger_menu); // Ustaw ikonę burger_menu
                isFabOpen = false;
            } else {
                // Otwórz FAB i zmień ikonę na cross
                fabAddRecipe.setVisibility(View.VISIBLE);
                fabMain.setImageResource(R.drawable.cross); // Ustaw ikonę cross
                isFabOpen = true;
            }
        });

        // Obsługa kliknięć poza przyciskami
        View mainLayout = findViewById(R.id.main_layout); // Zmieniamy ID głównego layoutu
        mainLayout.setOnClickListener(v -> {
            if (fabAddRecipe.getVisibility() == View.VISIBLE) {
                fabAddRecipe.setVisibility(View.GONE); // Zamknij menu
            }
        });

        // Listener przycisku dodawania przepisu
        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            startActivityForResult(intent, 1);
        });

        // Znajdź SwipeRefreshLayout
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // Listener do odświeżania przez przeciągnięcie w dół
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
                runOnUiThread(() -> {
                    recipes.clear();
                    recipes.addAll(dbRecipes);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false); // Zatrzymaj animację odświeżania

                    // Zamknij FAB i zmień ikonę
                    fabAddRecipe.setVisibility(View.GONE);
                    fabMain.setImageResource(R.drawable.burger_menu);
                    isFabOpen = false;
                });
            });
        });

        // Znajdź RecyclerView
        recyclerView = findViewById(R.id.recipes_recycler_view);
        int spanCount = getResources().getConfiguration().screenWidthDp > 600 ? 3 : 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        // Inicjalizacja listy przepisów
        recipes = new ArrayList<>();
        adapter = new RecipesAdapter(this, recipes);
        recyclerView.setAdapter(adapter);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (db.recipeDao().getAllRecipes().isEmpty()) {
                Recipe recipe1 = new Recipe("Chlebek bananowy", 30, "Pyszny chlebek z bananami", R.drawable.ic_placeholder);
                Recipe recipe2 = new Recipe("Makaron ze szpinakiem", 20, "Szybkie i zdrowe danie", R.drawable.ic_placeholder);

                // Logowanie przed wstawieniem
                Log.d("RecipeDebug", "Inserting recipe1: " + recipe1);
                Log.d("RecipeDebug", "Inserting recipe2: " + recipe2);

                db.recipeDao().insertRecipe(recipe1);
                db.recipeDao().insertRecipe(recipe2);
            }

            // Pobierz dane z bazy i zaktualizuj adapter
            List<Recipe> loadedRecipes = db.recipeDao().getAllRecipes();
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(loadedRecipes);
                adapter.notifyDataSetChanged();
            });
        });

        // Listener dla SearchView
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    List<Recipe> dbRecipes = AppDatabase.getInstance(RecipesActivity.this).recipeDao().getAllRecipes();
                    runOnUiThread(() -> {
                        if (newText.isEmpty()) {
                            // Przywróć wszystkie przepisy
                            recipes.clear();
                            recipes.addAll(dbRecipes);
                        } else {
                            // Filtruj przepisy
                            List<Recipe> filteredList = new ArrayList<>();
                            for (Recipe recipe : dbRecipes) {
                                if (recipe.getName().toLowerCase().contains(newText.toLowerCase())) {
                                    filteredList.add(recipe);
                                }
                            }
                            recipes.clear();
                            recipes.addAll(filteredList);
                        }
                        adapter.notifyDataSetChanged();
                    });
                });
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                closeFab(); // Zamknij rozwinięty FAB
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                closeFab(); // Zamknij rozwinięty FAB
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Odśwież listę przepisów
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(dbRecipes);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Odśwież listę przepisów
            Executors.newSingleThreadExecutor().execute(() -> {
                List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
                runOnUiThread(() -> {
                    recipes.clear();
                    recipes.addAll(dbRecipes);
                    adapter.notifyDataSetChanged();
                });
            });

            // Zamknij hamburger, jeśli jest otwarty
            FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
            fabAddRecipe.setVisibility(View.GONE);
        }
    }

    public void closeFab() {
        FloatingActionButton fabMain = findViewById(R.id.fab_main);
        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
        if (isFabOpen) {
            fabAddRecipe.setVisibility(View.GONE);
            fabMain.setImageResource(R.drawable.burger_menu); // Zmień ikonę na burger_menu
            isFabOpen = false;
        }
    }
}