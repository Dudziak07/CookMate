package com.example.cookmate.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cookmate.R;
import com.example.cookmate.database.PreparationStep;
import java.util.List;

public class PreparationStepsAdapterForDetails extends RecyclerView.Adapter<PreparationStepsAdapterForDetails.StepViewHolder> {

    private List<PreparationStep> steps;

    public PreparationStepsAdapterForDetails(List<PreparationStep> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_step_details, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        PreparationStep step = steps.get(position);

        // Wyświetlenie kroku przygotowania z numeracją
        holder.stepText.setText(step.getStepDescription());
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    public static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView stepText;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            stepText = itemView.findViewById(R.id.step_text);
        }
    }
}