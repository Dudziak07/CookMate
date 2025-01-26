package com.example.cookmate.view;

import android.content.Context;
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
    private Context context;

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
        Log.d("RecipesAdapter", "Displaying recipe: " + recipe.getName() + ", Tag: " + recipe.getTag());
        holder.nameTextView.setText(recipe.getName() != null ? recipe.getName() : "Brak nazwy");
        holder.timeTextView.setText(recipe.getPreparationTime() + " minut");

        // Sprawdź, czy tag istnieje
        if (recipe.getTag() != null && !recipe.getTag().isEmpty()) {
            holder.tagTextView.setText("#" + recipe.getTag());
            holder.tagTextView.setVisibility(View.VISIBLE);
        } else {
            holder.tagTextView.setVisibility(View.GONE); // Ukryj tag
        }

        Glide.with(holder.imageView.getContext())
                .load(recipe.getImageResourceId())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (context instanceof RecipesActivity) {
                ((RecipesActivity) context).closeFab();
            }
            Intent intent = new Intent(context, RecipeDetailsActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            context.startActivity(intent);
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

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView timeTextView;
        TextView tagTextView; // Dodano tagTextView
        ImageView imageView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.recipe_name);
            timeTextView = itemView.findViewById(R.id.recipe_time);
            tagTextView = itemView.findViewById(R.id.recipe_tag); // Inicjalizacja tagTextView
            imageView = itemView.findViewById(R.id.recipe_image);
        }
    }
}