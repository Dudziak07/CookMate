package com.example.cookmate.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.cookmate.database.Recipe;
import com.example.cookmate.database.RecipeDao;

@Database(entities = {Recipe.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract RecipeDao recipeDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "recipe_database")
                            .addMigrations(MIGRATION_2_3) // Dodaj migrację z wersji 2 do wersji 3
                            .fallbackToDestructiveMigration() // Opcjonalne, dla destrukcyjnej migracji w razie braku poprawnej
                            .build();
                }
            }
        }
        return instance;
    }

    // Migracja z wersji 2 do wersji 3
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Dodaj nową kolumnę na tagi
            database.execSQL("ALTER TABLE Recipe ADD COLUMN tag TEXT");
        }
    };
}