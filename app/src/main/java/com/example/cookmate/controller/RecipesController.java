package com.example.cookmate.controller;

import android.content.Context;

import androidx.room.Room;

import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;

import java.util.List;

public class RecipesController {
    private AppDatabase db;

    public RecipesController(Context context) {
        db = Room.databaseBuilder(context, AppDatabase.class, "cookmate-db")
                .fallbackToDestructiveMigration()
                .build();
    }

    public void addRecipe(Recipe recipe) {
        new Thread(() -> db.recipeDao().insertRecipe(recipe)).start();
    }

    public List<Recipe> getRecipes() {
        return db.recipeDao().getAllRecipes();
    }
}