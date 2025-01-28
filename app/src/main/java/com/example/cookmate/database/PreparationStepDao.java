package com.example.cookmate.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PreparationStepDao {
    @Insert
    void insertPreparationStep(PreparationStep step);
    @Insert
    void insertStep(PreparationStep step);

    @Query("SELECT * FROM PreparationStep WHERE recipeId = :recipeId")
    List<PreparationStep> getPreparationStepsForRecipe(int recipeId);

    @Query("SELECT * FROM PreparationStep WHERE recipeId = :recipeId")
    List<PreparationStep> getStepsForRecipe(int recipeId);

    @Query("DELETE FROM PreparationStep WHERE recipeId = :recipeId")
    void deleteStepsForRecipe(int recipeId);
}