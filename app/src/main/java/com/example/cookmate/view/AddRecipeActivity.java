package com.example.cookmate.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Ingredient;
import com.example.cookmate.database.Recipe;
import com.example.cookmate.database.RecipeImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AddRecipeActivity extends AppCompatActivity {
    // Stałe identyfikatory dla kodów żądań
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private static final int GALLERY_PERMISSION_CODE = 201;
    private static final int CAMERA_PERMISSION_CODE = 202;

    private List<Ingredient> ingredientsList = new ArrayList<>();

    private List<RecipeImage> addedImages = new ArrayList<>();
    private RecipeImagesAdapter imagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Inicjalizacja elementów interfejsu
        EditText nameInput = findViewById(R.id.recipe_name_input);
        EditText timeInput = findViewById(R.id.recipe_time_input);
        EditText descriptionInput = findViewById(R.id.recipe_description_input);
        EditText tagInput = findViewById(R.id.recipe_tag_input);
        Button saveButton = findViewById(R.id.save_recipe_button);
        Button addGalleryImage = findViewById(R.id.add_gallery_image);
        Button addCameraImage = findViewById(R.id.add_camera_image);

        setupRecyclerView();

        // Pola do dodawania składników
        EditText ingredientNameInput = findViewById(R.id.ingredient_name_input);
        EditText ingredientQuantityInput = findViewById(R.id.ingredient_quantity_input);
        Spinner ingredientUnitSpinner = findViewById(R.id.ingredient_unit_spinner);
        Button addIngredientButton = findViewById(R.id.add_ingredient_button);
        RecyclerView ingredientsRecyclerView = findViewById(R.id.ingredients_recycler_view);

        // Ustaw jednostki dla Spinnera
        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(this,
                R.array.default_units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ingredientUnitSpinner.setAdapter(unitAdapter);

        // Lista składników i adapter RecyclerView
        IngredientsAdapterForAdd ingredientsAdapter = new IngredientsAdapterForAdd(ingredientsList);
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientsRecyclerView.setAdapter(ingredientsAdapter);

        // Obsługa przycisku dodawania składników
        addIngredientButton.setOnClickListener(v -> {
            String name = ingredientNameInput.getText().toString().trim();
            String quantity = ingredientQuantityInput.getText().toString().trim();
            String unit = ingredientUnitSpinner.getSelectedItem().toString();

            if (name.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(this, "Podaj nazwę i ilość składnika", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double quantityValue = Double.parseDouble(quantity);

                // Tworzenie nowego składnika
                Ingredient ingredient = new Ingredient(name, quantityValue, unit);
                ingredientsList.add(ingredient);
                ingredientsAdapter.notifyDataSetChanged();

                // Czyszczenie pól wejściowych
                ingredientNameInput.setText("");
                ingredientQuantityInput.setText("");
                ingredientUnitSpinner.setSelection(0);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Podaj prawidłową ilość składnika", Toast.LENGTH_SHORT).show();
            }
        });

        // Dodaj ItemTouchHelper
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                ingredientsAdapter.moveItem(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Nie obsługujemy gestu przesuwania na bok
            }
        });
        itemTouchHelper.attachToRecyclerView(ingredientsRecyclerView);

        // Obsługa przycisku dodawania zdjęcia z galerii
        addGalleryImage.setOnClickListener(v -> requestGalleryPermission());

        // Obsługa przycisku robienia zdjęcia aparatem
        addCameraImage.setOnClickListener(v -> requestCameraPermission());

        // Obsługa przycisku zapisywania przepisu
        saveButton.setOnClickListener(v -> saveRecipe(nameInput, timeInput, descriptionInput, tagInput));
    }

    // Konfiguracja RecyclerView do wyświetlania zdjęć
    private void setupRecyclerView() {
        RecyclerView imagesRecyclerView = findViewById(R.id.images_recycler_view);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesAdapter = new RecipeImagesAdapter();
        imagesRecyclerView.setAdapter(imagesAdapter);
    }

    // Obsługa zapisywania przepisu
    private void saveRecipe(EditText nameInput, EditText timeInput, EditText descriptionInput, EditText tagInput) {
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String tag = tagInput.getText().toString().trim();
        Integer time = null; // Domyślna wartość to null

        // Walidacja nazwy przepisu
        if (name.isEmpty()) {
            Toast.makeText(this, "Nazwa przepisu nie może być pusta!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Przypisanie czasu, jeśli podano wartość
        if (!timeInput.getText().toString().trim().isEmpty()) {
            try {
                time = Integer.parseInt(timeInput.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Podaj prawidłowy czas przygotowania!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Ustawienie tagu na null, jeśli jest pusty
        if (tag.isEmpty()) {
            tag = null;
        }

        // Tworzenie obiektu przepisu i zapis do bazy danych
        Recipe recipe = new Recipe(name, time, description, R.drawable.ic_placeholder, tag);
        saveRecipeToDatabase(recipe);
    }

    // Walidacja danych wejściowych
    private boolean validateRecipeInput(String name, EditText timeInput) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Nazwa przepisu nie może być pusta!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (timeInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Podaj czas przygotowania!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Zapisanie przepisu do bazy danych
    private void saveRecipeToDatabase(Recipe recipe) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                long recipeId = AppDatabase.getInstance(this).recipeDao().insertRecipe(recipe);

                // Zapis składników
                for (Ingredient ingredient : ingredientsList) { // Używaj globalnej listy ingredientsList
                    ingredient.setRecipeId((int) recipeId);
                    AppDatabase.getInstance(this).ingredientDao().insertIngredient(ingredient);
                }

                // Zapis zdjęć do bazy danych
                for (RecipeImage image : addedImages) {
                    image.setRecipeId((int) recipeId);
                    AppDatabase.getInstance(this).recipeImageDao().insertImage(image);
                }

                runOnUiThread(() -> {
                    Toast.makeText(this, "Przepis zapisany!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Błąd podczas zapisu przepisu!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Obsługa wyniku aktywności (galeria/aparat)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri selectedImage = data.getData();
                addedImages.add(new RecipeImage(selectedImage.toString(), 0));
                imagesAdapter.setImages(addedImages);
            } else if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                Uri photoUri = savePhotoToInternalStorage(photo);
                addedImages.add(new RecipeImage(photoUri.toString(), 0));
                imagesAdapter.setImages(addedImages);
            }
        }
    }

    // Zapis zdjęcia do wewnętrznej pamięci
    private Uri savePhotoToInternalStorage(Bitmap photo) {
        File directory = getFilesDir();
        File photoFile = new File(directory, System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
            photo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(photoFile);
    }

    // Żądanie uprawnień do galerii
    private void requestGalleryPermission() {
        String permission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (checkPermission(permission, GALLERY_PERMISSION_CODE)) {
            openGallery();
        }
    }

    // Żądanie uprawnień do aparatu
    private void requestCameraPermission() {
        if (checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) {
            openCamera();
        }
    }

    // Sprawdzenie i żądanie uprawnień
    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            return false;
        }
    }

    // Otwarcie galerii
    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
    }

    // Otwarcie aparatu
    private void openCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
    }

    // Obsługa wyniku żądania uprawnień
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == GALLERY_PERMISSION_CODE) {
                openGallery();
            } else if (requestCode == CAMERA_PERMISSION_CODE) {
                openCamera();
            }
        } else {
            Toast.makeText(this, "Brak wymaganych uprawnień!", Toast.LENGTH_SHORT).show();
        }
    }
}
