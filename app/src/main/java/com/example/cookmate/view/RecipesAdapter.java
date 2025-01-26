package com.example.cookmate.view;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookmate.R;
import com.example.cookmate.database.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipesAdapter extends RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder> {
    private List<Recipe> recipes;
    private List<Recipe> originalRecipes;

    public RecipesAdapter(List<Recipe> recipes) {
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
        holder.nameTextView.setText(recipe.getName());
        holder.timeTextView.setText(recipe.getPreparationTime() + " minut"); // Dodanie "minut"
        Glide.with(holder.imageView.getContext())
                .load(recipe.getImageResourceId())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Log.d("RecipesAdapter", "Clicked recipe ID: " + recipe.getId());
            Intent intent = new Intent(v.getContext(), RecipeDetailsActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void filter(String text) {
        if (text.isEmpty()) {
            recipes = new ArrayList<>(originalRecipes); // Przywracanie oryginalnej listy
        } else {
            List<Recipe> filteredList = new ArrayList<>();
            for (Recipe recipe : originalRecipes) {
                if (recipe.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(recipe);
                }
            }
            recipes = filteredList;
        }
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView timeTextView;
        ImageView imageView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.recipe_name);
            timeTextView = itemView.findViewById(R.id.recipe_time);
            imageView = itemView.findViewById(R.id.recipe_image);
        }
    }
}