package com.example.cookmate.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookmate.R;

import java.util.Collections;
import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder> {

    private List<String> ingredients;

    public IngredientsAdapter(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        String ingredient = ingredients.get(position);
        holder.ingredientText.setText(ingredient);

        // Usuwanie składnika po kliknięciu na ikonę kosza
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

    // Przesuwanie składników
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