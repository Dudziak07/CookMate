package com.example.cookmate.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;
import com.example.cookmate.database.RecipeImage;

import java.util.List;
import java.util.concurrent.Executors;

public class RecipeDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        // Pobierz ID przepisu z Intent
        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        Log.d("RecipeDetailsActivity", "Received recipe ID: " + recipeId);

        // Zainicjalizuj widoki
        TextView recipeName = findViewById(R.id.recipe_name);
        TextView recipeTime = findViewById(R.id.recipe_time);
        TextView recipeDescription = findViewById(R.id.recipe_description);
        TextView tagTextView = findViewById(R.id.recipe_tag);

        // Wykonaj operację na bazie danych w wątku w tle
        Executors.newSingleThreadExecutor().execute(() -> {
            Recipe recipe = AppDatabase.getInstance(this).recipeDao().getRecipeById(recipeId);

            // Obsłuż brak przepisu
            if (recipe == null) {
                Log.e("RecipeDetailsActivity", "Recipe not found for ID: " + recipeId);
            } else {
                Log.d("RecipeDetailsActivity", "Loaded recipe: " + recipe.getName());
            }

            // Zaktualizuj interfejs użytkownika w wątku głównym
            runOnUiThread(() -> {
                if (recipe != null) {
                    recipeName.setText(recipe.getName());
                    recipeTime.setText(recipe.getPreparationTime() + " minut");
                    recipeDescription.setText(recipe.getDescription());

                    if (recipe.getTag() != null && !recipe.getTag().isEmpty()) {
                        tagTextView.setText("#" + recipe.getTag());
                        tagTextView.setVisibility(View.VISIBLE);
                    } else {
                        tagTextView.setVisibility(View.GONE);
                    }
                } else {
                    recipeName.setText("Nie znaleziono przepisu");
                }
            });
        });

        RecyclerView imagesRecyclerView = findViewById(R.id.images_recycler_view);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        RecipeImagesAdapter imagesAdapter = new RecipeImagesAdapter();
        imagesRecyclerView.setAdapter(imagesAdapter);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<RecipeImage> images = AppDatabase.getInstance(this).recipeImageDao().getImagesForRecipe(recipeId);
            runOnUiThread(() -> {
                if (images != null && !images.isEmpty()) {
                    imagesAdapter.setImages(images);
                }
            });
        });
    }
}
