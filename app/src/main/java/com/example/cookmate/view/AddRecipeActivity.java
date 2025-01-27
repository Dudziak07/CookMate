package com.example.cookmate.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private static final int GALLERY_PERMISSION_CODE = 201;
    private static final int STORAGE_PERMISSION_CODE = 201;
    private static final int CAMERA_PERMISSION_CODE = 202;

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

        // Obsługa przycisku do dodawania zdjęcia z galerii
        addGalleryImage.setOnClickListener(v -> {
            String permission = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;
            if (checkPermission(permission, GALLERY_PERMISSION_CODE)) {
                openGallery();
            }
        });

        // Obsługa przycisku do robienia zdjęcia aparatem
        addCameraImage.setOnClickListener(v -> {
            if (checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)) {
                openCamera();
            }
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

    // Metoda do sprawdzania uprawnień
    private boolean checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            // W przypadku Androida 13 obsłuż nowe uprawnienie READ_MEDIA_IMAGES
            if (permission.equals(Manifest.permission.READ_MEDIA_IMAGES)
                    && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, requestCode);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
            return false;
        }
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
            if (requestCode == GALLERY_PERMISSION_CODE) {
                Toast.makeText(this, "Uprawnienia do odczytu mediów są wymagane!", Toast.LENGTH_SHORT).show();
            } else if (requestCode == CAMERA_PERMISSION_CODE) {
                Toast.makeText(this, "Uprawnienia do aparatu są wymagane!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
    }

    private void openCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE);
    }
}