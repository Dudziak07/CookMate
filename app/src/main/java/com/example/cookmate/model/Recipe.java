package com.example.cookmate.model;

public class Recipe {
    private String name;
    private String preparationTime;
    private int imageResourceId;

    public Recipe(String name, String preparationTime, int imageResourceId) {
        this.name = name;
        this.preparationTime = preparationTime;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public String getPreparationTime() {
        return preparationTime;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}