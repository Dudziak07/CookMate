package com.example.cookmate.view;

import android.os.Bundle;
import androidx.appcompat.widget.SearchView; // Dodaj import SearchView
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookmate.R;
import com.example.cookmate.database.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipesAdapter adapter;
    private List<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        // Znajdź RecyclerView
        recyclerView = findViewById(R.id.recipes_recycler_view);
        int spanCount = getResources().getConfiguration().screenWidthDp > 600 ? 3 : 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        // Inicjalizacja listy przepisów (dla testów można wstawić dane na sztywno)
        recipes = new ArrayList<>();
        recipes.add(new Recipe("Chlebek bananowy", "30 minut", "Pyszny chlebek z bananów", R.drawable.ic_placeholder));
        recipes.add(new Recipe("Makaron ze szpinakiem", "20 minut", "Szybkie i zdrowe danie", R.drawable.ic_placeholder));

        // Ustaw Adapter
        adapter = new RecipesAdapter(recipes);
        recyclerView.setAdapter(adapter);

        // Listener dla SearchView
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

    }
}