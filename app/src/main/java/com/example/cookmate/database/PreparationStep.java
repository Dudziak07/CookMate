package com.example.cookmate.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class PreparationStep {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int recipeId;
    private String stepDescription;

    // Konstruktor używany przez Room
    public PreparationStep(int recipeId, String stepDescription) {
        this.recipeId = recipeId;
        this.stepDescription = stepDescription;
    }

    // Konstruktor ignorowany przez Room
    @Ignore
    public PreparationStep(String stepDescription) {
        this.stepDescription = stepDescription;
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

    public String getStepDescription() {
        return stepDescription;
    }

    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }
}