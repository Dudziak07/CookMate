package com.example.cookmate.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;
import com.example.cookmate.database.RecipeImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.Calendar;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private List<Recipe> originalRecipes;
    private Context context;

    private boolean isSelectionMode = false;
    private List<Recipe> selectedRecipes = new ArrayList<>();

    public RecipesAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.recipes = recipes;
        this.originalRecipes = new ArrayList<>(recipes);
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        Log.d("RecipesAdapter", "Displaying recipe: " + recipe.getName() +
                ", ImageResourceId: " + recipe.getImageResourceId() + ", Tag: " + recipe.getTag());

        holder.nameTextView.setText(recipe.getName() != null ? recipe.getName() : "Brak nazwy");

        // Ustawianie czasu przygotowania
        if (recipe.getPreparationTime() != null) {
            SpannableString spannableString = new SpannableString("  " + recipe.getPreparationTime() + " minut");
            Drawable clockIcon = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_clock);
            if (clockIcon != null) {
                int iconSize = (int) (holder.timeTextView.getTextSize());
                clockIcon.setBounds(0, 0, iconSize, iconSize);
                clockIcon.setTint(ContextCompat.getColor(holder.itemView.getContext(), R.color.mango_tango));
                ImageSpan imageSpan = new ImageSpan(clockIcon, ImageSpan.ALIGN_BASELINE);
                spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }

            holder.timeTextView.setText(spannableString);
            holder.timeTextView.setVisibility(View.VISIBLE);
        } else {
            holder.timeTextView.setVisibility(View.GONE);
        }

        // Ustawienie widoczności checkboxa
        holder.checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setChecked(selectedRecipes.contains(recipe));

        // Poprawiona obsługa checkboxa - działa nawet jak RecyclerView recyklinguje widoki
        holder.checkBox.setOnCheckedChangeListener(null); // Usuń stary listener, aby uniknąć błędów
        holder.checkBox.setChecked(selectedRecipes.contains(recipe));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedRecipes.contains(recipe)) {
                    selectedRecipes.add(recipe);
                }
            } else {
                selectedRecipes.remove(recipe);
            }
            Log.d("RecipesAdapter", "Selected Recipes: " + selectedRecipes.size());
        });

        // Pobierz pierwsze zdjęcie przypisane do przepisu
        Executors.newSingleThreadExecutor().execute(() -> {
            List<RecipeImage> images = AppDatabase.getInstance(context).recipeImageDao().getImagesForRecipe(recipe.getId());
            holder.imageView.post(() -> {
                if (!images.isEmpty()) {
                    Glide.with(holder.imageView.getContext())
                            .load(Uri.parse(images.get(0).getImageUri()))
                            .placeholder(R.drawable.ic_placeholder)
                            .into(holder.imageView);
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_placeholder);
                }
            });
        });

        // Obsługa tagów
        if (recipe.getTag() != null && !recipe.getTag().isEmpty()) {
            holder.tagTextView.setText("#" + recipe.getTag());
            holder.tagTextView.setVisibility(View.VISIBLE);
        } else {
            holder.tagTextView.setVisibility(View.GONE);
        }

        // Obsługa kliknięcia na przepis (ale tylko jeśli nie jest w trybie wyboru!)
        holder.itemView.setOnClickListener(v -> {
            if (!isSelectionMode) {
                Intent intent = new Intent(context, RecipeDetailsActivity.class);
                intent.putExtra("RECIPE_ID", recipe.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void filter(String text) {
        if (text.isEmpty()) {
            // Przywracaj pełną listę, niezależnie od tagów
            recipes = new ArrayList<>(originalRecipes);
        } else {
            List<Recipe> filteredList = new ArrayList<>();
            for (Recipe recipe : originalRecipes) {
                // Sprawdź, czy nazwa lub tag pasują do tekstu wyszukiwania
                if ((recipe.getName() != null && recipe.getName().toLowerCase().contains(text.toLowerCase())) ||
                        (recipe.getTag() != null && recipe.getTag().toLowerCase().contains(text.toLowerCase()))) {
                    filteredList.add(recipe);
                }
            }
            recipes = filteredList;
        }
        notifyDataSetChanged();
    }

    public void sortRecipesAlphabetically() {
        Collections.sort(recipes, (r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView timeTextView;
        TextView tagTextView;
        ImageView imageView;
        CheckBox checkBox; // Dodaj to pole

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.recipe_name);
            timeTextView = itemView.findViewById(R.id.recipe_time);
            tagTextView = itemView.findViewById(R.id.recipe_tag);
            imageView = itemView.findViewById(R.id.recipe_image);
            checkBox = itemView.findViewById(R.id.recipe_checkbox); // Dodaj to
        }
    }

    public void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;
        selectedRecipes.clear();
        notifyDataSetChanged();
    }

    public List<Recipe> getSelectedRecipes() {
        return new ArrayList<>(selectedRecipes);
    }
}