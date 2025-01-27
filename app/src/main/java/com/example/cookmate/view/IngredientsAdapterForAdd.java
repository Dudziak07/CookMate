package com.example.cookmate.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookmate.R;
import com.example.cookmate.database.Ingredient;

import java.util.Collections;
import java.util.List;

public class IngredientsAdapterForAdd extends RecyclerView.Adapter<IngredientsAdapterForAdd.IngredientViewHolder> {

    private List<Ingredient> ingredients;

    public IngredientsAdapterForAdd(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_add, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);

        // Sprawdź, czy ilość jest liczbą całkowitą
        String quantity = (ingredient.getQuantity() % 1 == 0)
                ? String.valueOf((int) ingredient.getQuantity()) // Usuń .0
                : String.valueOf(ingredient.getQuantity());

        holder.ingredientText.setText(ingredient.getName() + " - " + quantity + " " + ingredient.getUnit());

        // Obsługa przycisku usuwania
        holder.deleteButton.setOnClickListener(v -> {
            ingredients.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, ingredients.size());
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    // Handle item moving (drag-and-drop)
    public void moveItem(int fromPosition, int toPosition) {
        Collections.swap(ingredients, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientText;
        ImageView deleteButton;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            ingredientText = itemView.findViewById(R.id.ingredient_text);
            deleteButton = itemView.findViewById(R.id.delete_ingredient_button);
        }
    }
}
