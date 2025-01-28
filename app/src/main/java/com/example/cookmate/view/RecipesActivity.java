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
import com.example.cookmate.utils.GoogleCalendarHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class RecipesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipesAdapter adapter;
    private List<Recipe> recipes;
    private FloatingActionButton fabMain, fabAddRecipe, fabAddToCalendar;
    private boolean isFabOpen = false; // Status rozwijanego menu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        // Znajdź elementy FAB
        fabMain = findViewById(R.id.fab_main);
        fabAddRecipe = findViewById(R.id.fab_add_recipe);
        fabAddToCalendar = findViewById(R.id.fab_add_to_calendar);

        // Kliknięcie głównego FAB
        fabMain.setOnClickListener(v -> toggleFabMenu());

        // Kliknięcie przycisku "Dodaj przepis"
        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(RecipesActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });

        // Kliknięcie przycisku "Dodaj do Kalendarza"
        fabAddToCalendar.setOnClickListener(v -> {
            addEventToCalendar();
        });

        // Obsługa kliknięć poza przyciskami
        View mainLayout = findViewById(R.id.main_layout); // Zmieniamy ID głównego layoutu
        mainLayout.setOnClickListener(v -> closeFab());

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
            List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
            Log.d("RecipesActivity", "Loaded recipes: " + dbRecipes.size());
            for (Recipe recipe : dbRecipes) {
                Log.d("RecipesActivity", "Recipe: " + recipe.getName() + ", Tag: " + recipe.getTag());
            }
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(dbRecipes);
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

        // Pobierz SearchView
        SearchView searchView = findViewById(R.id.search_view);

        // Zamknij SearchView i ukryj klawiaturę
        if (searchView != null) {
            searchView.clearFocus(); // Usuwa fokus z SearchView
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(dbRecipes);
                adapter.notifyDataSetChanged();
            });
        });
        closeFab(); // Upewnij się, że FAB jest zamknięty
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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_sort_alphabetically) {
            sortRecipesAlphabetically();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortRecipesAlphabetically() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes(); // Już posortowane w SQL
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(dbRecipes);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void toggleFabMenu() {
        if (isFabOpen) {
            fabAddRecipe.setVisibility(View.GONE);
            fabAddToCalendar.setVisibility(View.GONE);
            fabMain.setImageResource(R.drawable.burger_menu); // Ustaw ikonę na burger_menu
            isFabOpen = false;
        } else {
            fabAddRecipe.setVisibility(View.VISIBLE);
            fabAddToCalendar.setVisibility(View.VISIBLE);
            fabMain.setImageResource(R.drawable.cross); // Ustaw ikonę na krzyżyk
            isFabOpen = true;
        }
    }

    private void addEventToCalendar() {
        String title = "Przygotowanie dania";
        String description = "Zaplanuj przygotowanie dania z CookMate";
        int preparationTime = 70; // np. 70 minut (później można pobierać dynamicznie)

        GoogleCalendarHelper.addEventToGoogleCalendar(this, title, description, preparationTime);
    }
}