package com.example.cookmate.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;

public class AddRecipeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        EditText nameInput = findViewById(R.id.recipe_name_input);
        EditText timeInput = findViewById(R.id.recipe_time_input);
        EditText descriptionInput = findViewById(R.id.recipe_description_input);
        Button saveButton = findViewById(R.id.save_recipe_button);

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String time = timeInput.getText().toString();
            String description = descriptionInput.getText().toString();

            Recipe recipe = new Recipe(name, time, description, R.drawable.ic_placeholder);
            AppDatabase.getInstance(this).recipeDao().insertRecipe(recipe);

            Toast.makeText(this, "Przepis zapisany!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}