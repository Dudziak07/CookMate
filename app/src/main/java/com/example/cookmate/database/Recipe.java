package com.example.cookmate.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String preparationTime;
    private String description;
    private int imageResourceId; // dla obrazów
    private List<String> tags; // opcjonalne tagi

    // Konstruktor
    public Recipe(String name, String preparationTime, String description, int imageResourceId) {
        this.name = name;
        this.preparationTime = preparationTime;
        this.description = description;
        this.imageResourceId = imageResourceId;
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
        this.name = name;
    }

    public String getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(String preparationTime) {
        this.preparationTime = preparationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}