package com.example.cookmate.database;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;

    @Nullable
    private Integer preparationTime; // Zmieniono na Integer, aby akceptować null

    private String description = "";
    private int imageResourceId;

    @Nullable
    private String tag;

    @Nullable
    private String imagePath; // Nowe pole do przechowywania ścieżki obrazu

    // Konstruktor bezargumentowy (wymagany przez Room)
    public Recipe() {
    }

    // Konstruktor
    public Recipe(String name, @Nullable Integer preparationTime, String description, int imageResourceId, String tag) {
        this.name = name != null ? name : "Brak nazwy";
        this.preparationTime = preparationTime; // Może być null
        this.description = description != null ? description : "Brak opisu";
        this.imageResourceId = imageResourceId;
        this.tag = tag != null && !tag.isEmpty() ? tag : null; // Jeśli brak tagu, ustaw na null
        this.imagePath = imagePath; // Ścieżka do obrazu
    }

    // Gettery i settery
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "Brak nazwy";
    }

    @Nullable
    public Integer getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(@Nullable Integer preparationTime) {
        this.preparationTime = preparationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "Brak opisu";
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    @Nullable
    public String getTag() {
        return tag;
    }

    public void setTag(@Nullable String tag) {
        this.tag = tag;
    }

    @Nullable
    public String getImagePath() { // Getter do ścieżki obrazu
        return imagePath;
    }

    public void setImagePath(@Nullable String imagePath) { // Setter do ścieżki obrazu
        this.imagePath = imagePath;
    }
}
