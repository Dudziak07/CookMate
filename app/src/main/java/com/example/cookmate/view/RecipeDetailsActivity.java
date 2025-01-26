package com.example.cookmate.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;
import java.util.concurrent.Executors;

public class RecipeDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        Log.d("RecipeDetailsActivity", "Received recipe ID: " + recipeId);

        TextView recipeName = findViewById(R.id.recipe_name);
        TextView recipeTime = findViewById(R.id.recipe_time);
        TextView recipeDescription = findViewById(R.id.recipe_description);

        // Wykonaj operację na bazie danych w tle
        Executors.newSingleThreadExecutor().execute(() -> {
            Recipe recipe = AppDatabase.getInstance(this).recipeDao().getRecipeById(recipeId);

            if (recipe == null) {
                Log.e("RecipeDetailsActivity", "Recipe not found for ID: " + recipeId);
            } else {
                Log.d("RecipeDetailsActivity", "Loaded recipe: " + recipe.getName());
            }

            runOnUiThread(() -> {
                if (recipe != null) {
                    recipeName.setText(recipe.getName());
                    recipeTime.setText(recipe.getPreparationTime() + " minut");
                    recipeDescription.setText(recipe.getDescription());
                } else {
                    recipeName.setText("Nie znaleziono przepisu");
                }
            });
        });
    }
}