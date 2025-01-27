package com.example.cookmate.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;
import com.example.cookmate.database.RecipeImage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AddRecipeActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;

    private List<RecipeImage> addedImages = new ArrayList<>();
    private RecipeImagesAdapter imagesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        EditText nameInput = findViewById(R.id.recipe_name_input);
        EditText timeInput = findViewById(R.id.recipe_time_input);
        EditText descriptionInput = findViewById(R.id.recipe_description_input);
        EditText tagInput = findViewById(R.id.recipe_tag_input);
        Button saveButton = findViewById(R.id.save_recipe_button);

        // Obsługa przycisków dodawania zdjęć
        Button addGalleryImage = findViewById(R.id.add_gallery_image);
        Button addCameraImage = findViewById(R.id.add_camera_image);

        RecyclerView imagesRecyclerView = findViewById(R.id.images_recycler_view);
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesAdapter = new RecipeImagesAdapter();
        imagesRecyclerView.setAdapter(imagesAdapter);

        // Listener do dodawania zdjęcia z galerii
        addGalleryImage.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
        });

        // Listener do robienia zdjęcia aparatem
        addCameraImage.setOnClickListener(v -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
        });

        // Listener przycisku zapisu przepisu
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

            if (tag.isEmpty()) {
                tag = null;
            }

            Recipe recipe = new Recipe(name, time, description, R.drawable.ic_placeholder, tag);

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    long recipeId = AppDatabase.getInstance(this).recipeDao().insertRecipe(recipe);

                    // Zapisz dodane zdjęcia w bazie danych
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
        });
    }

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
}