package com.example.cookmate.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {
        Recipe.class, RecipeImage.class, Ingredient.class, PreparationStep.class
}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract RecipeDao recipeDao();
    public abstract IngredientDao ingredientDao();
    public abstract PreparationStepDao preparationStepDao();
    public abstract RecipeImageDao recipeImageDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "recipe_database")
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_6_7)
                            .fallbackToDestructiveMigration() // Usuwa dane, jeśli migracja zawiedzie (opcjonalne)
                            .build();
                }
            }
        }
        return instance;
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Dodaj nową kolumnę na tagi
            database.execSQL("ALTER TABLE Recipe ADD COLUMN tag TEXT");
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE Recipe_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "name TEXT NOT NULL, " +
                    "preparationTime INTEGER, " + // INTEGER pozwala na null
                    "description TEXT NOT NULL, " +
                    "imageResourceId INTEGER NOT NULL, " +
                    "tag TEXT)");
            database.execSQL("INSERT INTO Recipe_new (id, name, preparationTime, description, imageResourceId, tag) " +
                    "SELECT id, name, preparationTime, description, imageResourceId, tag FROM Recipe");
            database.execSQL("DROP TABLE Recipe");
            database.execSQL("ALTER TABLE Recipe_new RENAME TO Recipe");
        }
    };

    static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Usuń obecną tabelę, jeśli istnieje
            database.execSQL("DROP TABLE IF EXISTS PreparationStep");

            // Utwórz nową tabelę zgodną z oczekiwanym schematem
            database.execSQL("CREATE TABLE PreparationStep (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "recipeId INTEGER NOT NULL, " +
                    "stepDescription TEXT, " + // TEXT bez NOT NULL
                    "FOREIGN KEY(recipeId) REFERENCES Recipe(id) ON DELETE CASCADE)");
        }
    };
}

