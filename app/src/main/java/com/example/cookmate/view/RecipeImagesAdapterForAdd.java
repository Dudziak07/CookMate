package com.example.cookmate.view;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cookmate.database.RecipeImage;
import com.example.cookmate.R;

import java.util.ArrayList;
import java.util.List;

public class RecipeImagesAdapterForAdd extends RecyclerView.Adapter<RecipeImagesAdapterForAdd.ImageViewHolder> {
    private List<RecipeImage> images = new ArrayList<>();

    public void setImages(List<RecipeImage> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_add, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        RecipeImage image = images.get(position);

        if (image.getImageUri() != null) {
            Glide.with(holder.imageView.getContext())
                    .load(Uri.parse(image.getImageUri()))
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_placeholder);
        }

        // Obsługa usuwania zdjęcia
        holder.deleteButton.setOnClickListener(v -> {
            images.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, images.size());
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    // ViewHolder
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            deleteButton = itemView.findViewById(R.id.delete_image_button);
        }
    }
}