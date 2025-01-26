package com.example.cookmate.view;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;

public class RecipeDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);

        if (recipeId == -1) {
            Toast.makeText(this, "Nie znaleziono przepisu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Recipe recipe = AppDatabase.getInstance(this).recipeDao().getRecipeById(recipeId);

        if (recipe == null) {
            Toast.makeText(this, "Nie znaleziono przepisu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView recipeName = findViewById(R.id.recipe_name);
        TextView recipeTime = findViewById(R.id.recipe_time);
        TextView recipeDescription = findViewById(R.id.recipe_description);

        recipeName.setText(recipe.getName());
        recipeTime.setText(recipe.getPreparationTime() + " minut");
        recipeDescription.setText(recipe.getDescription());
    }
}
