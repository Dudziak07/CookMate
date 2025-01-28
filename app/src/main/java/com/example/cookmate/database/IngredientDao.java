package com.example.cookmate.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface IngredientDao {
    @Insert
    void insertIngredient(Ingredient ingredient);

    @Query("SELECT * FROM Ingredient WHERE recipeId = :recipeId")
    List<Ingredient> getIngredientsForRecipe(int recipeId);

    @Query("DELETE FROM Ingredient WHERE recipeId = :recipeId")
    void deleteIngredientsForRecipe(int recipeId);
}