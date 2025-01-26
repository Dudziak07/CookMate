package com.example.cookmate.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {
    @Insert
    void insertRecipe(Recipe recipe);

    @Query("SELECT * FROM recipes")
    List<Recipe> getAllRecipes();
}
