package com.example.cookmate.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class RecipeImage {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int recipeId;
    private String imageUri;

    public RecipeImage(String imageUri, int recipeId) {
        this.imageUri = imageUri;
        this.recipeId = recipeId;
    }

    // Gettery i settery
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}