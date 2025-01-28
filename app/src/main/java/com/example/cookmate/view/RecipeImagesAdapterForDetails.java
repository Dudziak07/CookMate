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

import java.util.List;

public class RecipeImagesAdapterForDetails extends RecyclerView.Adapter<RecipeImagesAdapterForDetails.ImageViewHolder> {
    private List<RecipeImage> images;

    public RecipeImagesAdapterForDetails(List<RecipeImage> images) {
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_details, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        RecipeImage image = images.get(position);

        if (image.getImageUri() != null) {
            Glide.with(holder.imageView.getContext())
                    .load(Uri.parse(image.getImageUri())) // Wczytaj URI obrazu
                    .placeholder(R.drawable.ic_placeholder) // Ustaw placeholder
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_placeholder); // Domyślny obraz
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
        }
    }
}