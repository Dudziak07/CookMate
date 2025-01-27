package com.example.cookmate.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeImageDao {
    @Insert
    void insertImage(RecipeImage recipeImage);

    @Query("SELECT * FROM RecipeImage WHERE recipeId = :recipeId")
    List<RecipeImage> getImagesForRecipe(int recipeId);

    @Delete
    void deleteImage(RecipeImage recipeImage);
}