package com.example.cookmate.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Ingredient;
import com.example.cookmate.database.PreparationStep;
import com.example.cookmate.database.Recipe;
import com.example.cookmate.database.RecipeImage;

import java.util.List;
import java.util.concurrent.Executors;

public class RecipeDetailsActivity extends AppCompatActivity {
    private int recipeId;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        invalidateOptionsMenu(); // Wymuszenie odświeżenia menu

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            finish(); // Po prostu zamyka aktywność i wraca do poprzedniej
        });

        recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        if (recipeId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                Recipe loadedRecipe = AppDatabase.getInstance(this).recipeDao().getRecipeById(recipeId);
                runOnUiThread(() -> {
                    recipe = loadedRecipe; // Możemy teraz przypisać wartość w wątku UI
                    updateUI(recipe); // Aktualizacja interfejsu użytkownika
                });
            });
        }

        ImageView editButton = findViewById(R.id.edit_recipe_button);
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailsActivity.this, AddRecipeActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId()); // Przekazujemy ID przepisu
            startActivity(intent);
        });

        ImageView deleteRecipeButton = findViewById(R.id.delete_recipe);
        deleteRecipeButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        // Pobierz ID przepisu z Intent
        recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
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

        // Użyj adaptera RecipeImagesAdapterForDetails
        final int currentRecipeId = recipeId; // Kopia zmiennej

        Executors.newSingleThreadExecutor().execute(() -> {
            List<RecipeImage> images = AppDatabase.getInstance(this).recipeImageDao().getImagesForRecipe(currentRecipeId);
            runOnUiThread(() -> {
                if (images != null && !images.isEmpty()) {
                    RecipeImagesAdapterForDetails imagesAdapter = new RecipeImagesAdapterForDetails(images);
                    imagesRecyclerView.setAdapter(imagesAdapter);
                } else {
                    Log.d("RecipeDetailsActivity", "Brak zdjęć dla przepisu ID: " + currentRecipeId);
                }
            });
        });

        // Obsługa listy składników
        RecyclerView ingredientsRecyclerView = findViewById(R.id.ingredients_recycler_view);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Ingredient> ingredients = AppDatabase.getInstance(this).ingredientDao().getIngredientsForRecipe(currentRecipeId);
            runOnUiThread(() -> {
                if (ingredients != null && !ingredients.isEmpty()) {
                    IngredientsAdapterForDetails adapter = new IngredientsAdapterForDetails(ingredients);
                    ingredientsRecyclerView.setAdapter(adapter);
                } else {
                    Log.d("RecipeDetailsActivity", "Brak składników dla przepisu ID: " + currentRecipeId);
                }
            });
        });

        // Obsługa listy kroków przygotowania
        RecyclerView stepsRecyclerView = findViewById(R.id.steps_recycler_view);
        stepsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Executors.newSingleThreadExecutor().execute(() -> {
            List<PreparationStep> preparationSteps = AppDatabase.getInstance(this)
                    .preparationStepDao().getStepsForRecipe(currentRecipeId);

            runOnUiThread(() -> {
                if (preparationSteps != null && !preparationSteps.isEmpty()) {
                    PreparationStepsAdapterForDetails adapter = new PreparationStepsAdapterForDetails(preparationSteps);
                    stepsRecyclerView.setAdapter(adapter);
                } else {
                    Log.d("RecipeDetailsActivity", "Brak kroków przygotowania dla przepisu ID: " + currentRecipeId);
                }
            });
        });
    }

    private void showDeleteConfirmationDialog() {
        Log.d("RecipeDetailsActivity", "Pokazano dialog usuwania"); // Debug
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Usuń przepis")
                .setMessage("Czy na pewno chcesz usunąć przepis?")
                .setPositiveButton("Tak, usuń", (dialogInterface, which) -> {
                    Log.d("RecipeDetailsActivity", "Kliknięto TAK w dialogu usuwania"); // Debug
                    deleteRecipe();
                })
                .setNegativeButton("Nie, anuluj", (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.warning));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, R.color.gray40));
        });

        dialog.show();
    }

    private void deleteRecipe() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            db.recipeDao().deleteRecipeById(recipeId); // Dodaj odpowiednią metodę w DAO
            runOnUiThread(() -> {
                Toast.makeText(this, "Przepis został usunięty", Toast.LENGTH_SHORT).show();
                finish(); // Powrót do poprzedniej aktywności
            });
        });
    }

    private void updateUI(Recipe recipe) {
        if (recipe == null) {
            Log.e("RecipeDetailsActivity", "Recipe not found for ID: " + recipeId);
            return;
        }

        TextView recipeName = findViewById(R.id.recipe_name);
        TextView recipeTime = findViewById(R.id.recipe_time);
        TextView recipeDescription = findViewById(R.id.recipe_description);
        ImageView recipeImage = findViewById(R.id.recipe_image);

        recipeName.setText(recipe.getName());
        recipeDescription.setText(recipe.getDescription());

        // Wyświetlanie czasu przygotowania
        if (recipe.getPreparationTime() != null) {
            recipeTime.setText("⏱ " + recipe.getPreparationTime() + " minut");
        } else {
            recipeTime.setVisibility(View.GONE);
        }

        // Pobieranie zdjęcia
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RecipeImage> images = AppDatabase.getInstance(this).recipeImageDao().getImagesForRecipe(recipe.getId());
            runOnUiThread(() -> {
                if (!images.isEmpty()) {
                    Glide.with(this)
                            .load(Uri.parse(images.get(0).getImageUri()))
                            .placeholder(R.drawable.ic_placeholder)
                            .into(recipeImage);
                } else {
                    recipeImage.setImageResource(R.drawable.ic_placeholder);
                }
            });
        });
    }

}
