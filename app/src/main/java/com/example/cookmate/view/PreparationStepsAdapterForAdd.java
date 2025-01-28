package com.example.cookmate.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookmate.R;
import com.example.cookmate.database.PreparationStep;
import java.util.Collections;
import java.util.List;

public class PreparationStepsAdapterForAdd extends RecyclerView.Adapter<PreparationStepsAdapterForAdd.StepViewHolder> {

    private List<PreparationStep> steps;

    public PreparationStepsAdapterForAdd(List<PreparationStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step_add, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        PreparationStep step = steps.get(position);
        holder.stepText.setText(step.getStepDescription());

        // Obsługa usuwania
        holder.deleteButton.setOnClickListener(v -> {
            steps.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, steps.size());
        });
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    // Obsługa przesuwania elementów
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(steps, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(steps, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepText;
        ImageView deleteButton;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepText = itemView.findViewById(R.id.step_text);
            deleteButton = itemView.findViewById(R.id.delete_step_button);
        }
    }
}