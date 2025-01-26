package com.example.cookmate.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

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
                });
            });
        });

        // Znajdź RecyclerView
        recyclerView = findViewById(R.id.recipes_recycler_view);
        int spanCount = getResources().getConfiguration().screenWidthDp > 600 ? 3 : 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        // Inicjalizacja listy przepisów
        recipes = new ArrayList<>();
        adapter = new RecipesAdapter(recipes);
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

        FloatingActionButton fabMain = findViewById(R.id.fab_main);
        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);

        fabMain.setOnClickListener(v -> {
            if (fabAddRecipe.getVisibility() == View.GONE) {
                fabAddRecipe.setVisibility(View.VISIBLE);
            } else {
                fabAddRecipe.setVisibility(View.GONE);
            }
        });

        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            startActivity(intent);
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
}