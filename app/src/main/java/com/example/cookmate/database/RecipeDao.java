package com.example.cookmate.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecipeDao {
    @Insert
    long insertRecipe(Recipe recipe); // Deklaracja metody dodającej przepis

    @Query("SELECT * FROM Recipe ORDER BY name ASC") // Wybiera wszystkie przepisy
    List<Recipe> getAllRecipes();

    @Query("SELECT * FROM Recipe WHERE id = :recipeId") // Wybiera przepis na podstawie ID
    Recipe getRecipeById(int recipeId);
}