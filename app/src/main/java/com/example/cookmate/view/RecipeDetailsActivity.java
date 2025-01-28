package com.example.cookmate.view;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Ingredient;
import com.example.cookmate.database.PreparationStep;
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

                    // Wyświetl czas przygotowania lub ukryj TextView, jeśli preparationTime jest null
                    if (recipe.getPreparationTime() != null) {
                        SpannableString spannableString = new SpannableString("  " + recipe.getPreparationTime() + " minut");

                        // Pobranie ikony zegara
                        Drawable clockIcon = ContextCompat.getDrawable(this, R.drawable.ic_clock);
                        if (clockIcon != null) {
                            int iconSize = (int) (recipeTime.getTextSize()); // Dopasowanie do tekstu
                            clockIcon.setBounds(0, 0, iconSize, iconSize);

                            // Zmiana koloru ikony
                            clockIcon.setTint(ContextCompat.getColor(this, R.color.mango_tango));

                            // Tworzymy ImageSpan, który wyrównuje ikonę do środka tekstu
                            ImageSpan imageSpan = new ImageSpan(clockIcon, ImageSpan.ALIGN_BASELINE);
                            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                        }

                        recipeTime.setText(spannableString);
                        recipeTime.setVisibility(View.VISIBLE);
                    } else {
                        recipeTime.setVisibility(View.GONE);
                    }

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

        // Obsługa listy składników
        RecyclerView ingredientsRecyclerView = findViewById(R.id.ingredients_recycler_view);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Ingredient> ingredients = AppDatabase.getInstance(this).ingredientDao().getIngredientsForRecipe(recipeId);

            runOnUiThread(() -> {
                if (ingredients != null && !ingredients.isEmpty()) {
                    // Użyj adaptera dla listy z punktorami
                    IngredientsAdapterForDetails adapter = new IngredientsAdapterForDetails(ingredients);
                    ingredientsRecyclerView.setAdapter(adapter);
                } else {
                    Log.d("RecipeDetailsActivity", "Brak składników dla przepisu ID: " + recipeId);
                }
            });
        });

        // Obsługa listy kroków przygotowania
        RecyclerView stepsRecyclerView = findViewById(R.id.steps_recycler_view);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Executors.newSingleThreadExecutor().execute(() -> {
            List<PreparationStep> preparationSteps = AppDatabase.getInstance(this)
                    .preparationStepDao().getStepsForRecipe(recipeId);

            runOnUiThread(() -> {
                if (preparationSteps != null && !preparationSteps.isEmpty()) {
                    PreparationStepsAdapterForDetails adapter = new PreparationStepsAdapterForDetails(preparationSteps);
                    stepsRecyclerView.setAdapter(adapter);
                } else {
                    Log.d("RecipeDetailsActivity", "Brak kroków przygotowania dla przepisu ID: " + recipeId);
                }
            });
        });
    }
}
