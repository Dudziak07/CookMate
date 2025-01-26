package com.example.cookmate.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int preparationTime; // int zamiast String
    private String description = "";
    private int imageResourceId; // dla obrazów
    private String tags; // Przechowujemy tagi jako String

    // Konstruktor bezargumentowy (wymagany przez Room)
    public Recipe() {
    }

    // Konstruktor
    public Recipe(String name, int preparationTime, String description, int imageResourceId) {
        this.name = name != null ? name : "Brak nazwy";
        this.preparationTime = preparationTime;
        this.description = description != null ? description : "Brak opisu";
        this.imageResourceId = imageResourceId;
        this.tags = ""; // Ustawienie wartości domyślnej
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

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}