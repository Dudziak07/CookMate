package com.example.cookmate.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;

import java.util.concurrent.Executors;

public class AddRecipeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        EditText nameInput = findViewById(R.id.recipe_name_input);
        EditText timeInput = findViewById(R.id.recipe_time_input);
        EditText descriptionInput = findViewById(R.id.recipe_description_input);
        EditText tagInput = findViewById(R.id.recipe_tag_input); // Nowe pole tagu
        Button saveButton = findViewById(R.id.save_recipe_button);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String tag = tagInput.getText().toString().trim();
            int time;

            if (name.isEmpty()) {
                Toast.makeText(this, "Nazwa przepisu nie może być pusta!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                time = Integer.parseInt(timeInput.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Podaj prawidłowy czas przygotowania!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Jeśli tag jest pusty, ustaw na null
            if (tag.isEmpty()) {
                tag = null;
            }

            Recipe recipe = new Recipe(name, time, description, R.drawable.ic_placeholder, tag);

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    AppDatabase.getInstance(this).recipeDao().insertRecipe(recipe);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Przepis zapisany!", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(this, "Błąd podczas zapisu przepisu!", Toast.LENGTH_SHORT).show());
                }
            });
        });
    }
}