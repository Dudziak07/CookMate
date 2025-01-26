package com.example.cookmate.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookmate.R;
import com.example.cookmate.model.Recipe;

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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Układ siatki z 2 kolumnami

        // Inicjalizacja listy przepisów (dla testów można wstawić dane na sztywno)
        recipes = new ArrayList<>();
        recipes.add(new Recipe("Chlebek bananowy", "30 minut", R.drawable.ic_placeholder));
        recipes.add(new Recipe("Makaron ze szpinakiem", "20 minut", R.drawable.ic_placeholder));

        // Ustaw Adapter
        adapter = new RecipesAdapter(recipes);
        recyclerView.setAdapter(adapter);
    }
}
