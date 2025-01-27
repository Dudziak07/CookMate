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

public class IngredientsAdapterForDetails extends RecyclerView.Adapter<IngredientsAdapterForDetails.IngredientViewHolder> {
    private List<Ingredient> ingredients;

    public IngredientsAdapterForDetails(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient_details, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);

        // Sprawdź, czy ilość jest liczbą całkowitą
        String quantity = (ingredient.getQuantity() % 1 == 0)
                ? String.valueOf((int) ingredient.getQuantity()) // Usuń .0
                : String.valueOf(ingredient.getQuantity());

        // Ustaw tekst z punktorami
        holder.nameTextView.setText("• " + ingredient.getName() + " - " + quantity + " " + ingredient.getUnit());
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.ingredient_name); // Sprawdź, czy ID jest poprawne
        }
    }
}
