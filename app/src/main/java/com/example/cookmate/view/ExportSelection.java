package com.example.cookmate.view;

import android.content.Intent;
import java.util.List;
import com.example.cookmate.database.Recipe;
import java.io.Serializable;

public class ExportSelection implements Serializable {
    private final Intent intent;
    private final List<Recipe> selectedRecipes;

    public ExportSelection(Intent intent, List<Recipe> selectedRecipes) {
        this.intent = intent;
        this.selectedRecipes = selectedRecipes;
    }

    public Intent getIntent() {
        return intent;
    }

    public List<Recipe> getSelectedRecipes() {
        return selectedRecipes;
    }
}
