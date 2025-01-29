package com.example.cookmate.view;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView; // Dodaj import SearchView
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.cookmate.R;
import com.example.cookmate.database.AppDatabase;
import com.example.cookmate.database.Recipe;
import com.example.cookmate.utils.GoogleCalendarHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class RecipesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecipesAdapter adapter;
    private List<Recipe> recipes;
    private FloatingActionButton fabMain, fabAddRecipe, fabAddToCalendar;
    private boolean isFabOpen = false; // Status rozwijanego menu
    private boolean isSelectionMode = false;

    private GoogleAccountCredential credential;
    private GoogleCalendarHelper googleCalendarHelper;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final String TAG = "RecipesActivity";
    private static final String CALENDAR_ID = "primary";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        // Inicjalizacja Google API
        credential = GoogleAccountCredential.usingOAuth2(
                        this, Collections.singleton(CalendarScopes.CALENDAR))
                .setBackOff(new ExponentialBackOff());

        googleCalendarHelper = new GoogleCalendarHelper(this, credential);

        // Sprawdź, czy mamy już wybrane konto Google
        String accountName = getPreferences(MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null) {
            credential.setSelectedAccountName(accountName);
            Log.d("RecipesActivity", "Załadowano zapisane konto: " + accountName);
        } else {
            Log.d("RecipesActivity", "Nie wybrano jeszcze konta Google.");
            chooseGoogleAccount();
        }

        requestCalendarPermissions();

        // Znajdź elementy FAB
        fabMain = findViewById(R.id.fab_main);
        fabAddRecipe = findViewById(R.id.fab_add_recipe);
        fabAddToCalendar = findViewById(R.id.fab_add_to_calendar);
        fabAddToCalendar.setImageDrawable(getDrawable(R.drawable.google_calendar_icon));

        // Kliknięcie głównego FAB
        fabMain.setOnClickListener(v -> toggleFabMenu());

        // Kliknięcie przycisku "Dodaj przepis"
        fabAddRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(RecipesActivity.this, AddRecipeActivity.class);
            startActivity(intent);
        });

        // Kliknięcie przycisku "Dodaj do Kalendarza"
        fabAddToCalendar.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR}, 200);
            } else {
                if (!isSelectionMode) {
                    isSelectionMode = true;
                    adapter.toggleSelectionMode();
                    fabAddToCalendar.setImageResource(R.drawable.ic_check);
                } else {
                    List<Recipe> selectedRecipes = adapter.getSelectedRecipes();
                    Log.d("CalendarDebug", "Liczba wybranych przepisów: " + selectedRecipes.size());
                    if (selectedRecipes.isEmpty()) {
                        Toast.makeText(this, "Wybierz co najmniej jeden przepis!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    showDateTimePicker();
                }
            }
        });

        // Obsługa kliknięć poza przyciskami
        View mainLayout = findViewById(R.id.main_layout); // Zmieniamy ID głównego layoutu
        mainLayout.setOnClickListener(v -> closeFab());

        // Znajdź SwipeRefreshLayout
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        // Listener do odświeżania przez przeciągnięcie w dół
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
                runOnUiThread(() -> {
                    recipes.clear();
                    recipes.addAll(dbRecipes);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false); // Zatrzymaj animację odświeżania

                    // Zamknij FAB i zmień ikonę
                    fabAddRecipe.setVisibility(View.GONE);
                    fabMain.setImageResource(R.drawable.burger_menu);
                    isFabOpen = false;
                });
            });
        });

        // Znajdź RecyclerView
        recyclerView = findViewById(R.id.recipes_recycler_view);
        int spanCount = getResources().getConfiguration().screenWidthDp > 600 ? 3 : 2;
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        // Inicjalizacja listy przepisów
        recipes = new ArrayList<>();
        adapter = new RecipesAdapter(this, recipes);
        recyclerView.setAdapter(adapter);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
            Log.d("RecipesActivity", "Loaded recipes: " + dbRecipes.size());
            for (Recipe recipe : dbRecipes) {
                Log.d("RecipesActivity", "Recipe: " + recipe.getName() + ", Tag: " + recipe.getTag());
            }
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(dbRecipes);
                adapter.notifyDataSetChanged();
            });
        });

        // Listener dla SearchView
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    List<Recipe> dbRecipes = AppDatabase.getInstance(RecipesActivity.this).recipeDao().getAllRecipes();
                    runOnUiThread(() -> {
                        if (newText.isEmpty()) {
                            // Przywróć wszystkie przepisy
                            recipes.clear();
                            recipes.addAll(dbRecipes);
                        } else {
                            // Filtruj przepisy
                            List<Recipe> filteredList = new ArrayList<>();
                            for (Recipe recipe : dbRecipes) {
                                if (recipe.getName().toLowerCase().contains(newText.toLowerCase())) {
                                    filteredList.add(recipe);
                                }
                            }
                            recipes.clear();
                            recipes.addAll(filteredList);
                        }
                        adapter.notifyDataSetChanged();
                    });
                });
                return true;
            }
        });

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                closeFab(); // Zamknij rozwinięty FAB
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                closeFab(); // Zamknij rozwinięty FAB
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Pobierz SearchView
        SearchView searchView = findViewById(R.id.search_view);

        // Zamknij SearchView i ukryj klawiaturę
        if (searchView != null) {
            searchView.clearFocus(); // Usuwa fokus z SearchView
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(dbRecipes);
                adapter.notifyDataSetChanged();
            });
        });
        closeFab(); // Upewnij się, że FAB jest zamknięty
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Odśwież listę przepisów
            Executors.newSingleThreadExecutor().execute(() -> {
                List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes();
                runOnUiThread(() -> {
                    recipes.clear();
                    recipes.addAll(dbRecipes);
                    adapter.notifyDataSetChanged();
                });
            });

            if (requestCode == REQUEST_AUTHORIZATION) {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "✅ Użytkownik przyznał dostęp do kalendarza.");
                    Toast.makeText(this, "Uprawnienia do kalendarza zatwierdzone! Spróbuj ponownie dodać wydarzenie.", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "❌ Użytkownik odmówił dostępu do kalendarza.");
                    Toast.makeText(this, "Nie przyznano uprawnień do kalendarza!", Toast.LENGTH_LONG).show();
                }
            }

            // Zamknij hamburger, jeśli jest otwarty
            FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
            fabAddRecipe.setVisibility(View.GONE);
        }
    }

    public void closeFab() {
        FloatingActionButton fabMain = findViewById(R.id.fab_main);
        FloatingActionButton fabAddRecipe = findViewById(R.id.fab_add_recipe);
        fabAddToCalendar = findViewById(R.id.fab_add_to_calendar);
        if (isFabOpen) {
            fabAddRecipe.setVisibility(View.GONE);
            fabAddToCalendar.setVisibility(View.GONE);
            fabMain.setImageResource(R.drawable.burger_menu); // Zmień ikonę na burger_menu
            isFabOpen = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_sort_alphabetically) {
            sortRecipesAlphabetically();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sortRecipesAlphabetically() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Recipe> dbRecipes = AppDatabase.getInstance(this).recipeDao().getAllRecipes(); // Już posortowane w SQL
            runOnUiThread(() -> {
                recipes.clear();
                recipes.addAll(dbRecipes);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void toggleFabMenu() {
        if (isFabOpen) {
            fabAddRecipe.setVisibility(View.GONE);
            fabAddToCalendar.setVisibility(View.GONE);
            fabMain.setImageResource(R.drawable.burger_menu); // Ustaw ikonę na burger_menu
            isFabOpen = false;
        } else {
            fabAddRecipe.setVisibility(View.VISIBLE);
            fabAddToCalendar.setVisibility(View.VISIBLE);
            fabMain.setImageResource(R.drawable.cross); // Ustaw ikonę na krzyżyk
            isFabOpen = true;
        }
    }

    @SuppressLint("StaticFieldLeak")
    public void addEventToGoogleCalendar(String title, String description, Date startTime, int durationMinutes) {
        new AsyncTask<Void, Void, String>() {
            private Exception error; // Dodaj to

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Event event = new Event()
                            .setSummary(title)
                            .setDescription(description);

                    Date endTime = new Date(startTime.getTime() + (durationMinutes * 60 * 1000));

                    EventDateTime startDateTime = new EventDateTime()
                            .setDateTime(new com.google.api.client.util.DateTime(startTime))
                            .setTimeZone(TimeZone.getDefault().getID());

                    EventDateTime endDateTime = new EventDateTime()
                            .setDateTime(new com.google.api.client.util.DateTime(endTime))
                            .setTimeZone(TimeZone.getDefault().getID());

                    event.setStart(startDateTime);
                    event.setEnd(endDateTime);

                    Log.d(TAG, "🔹 Tworzenie wydarzenia: " + event.toString());

                    Event addedEvent = googleCalendarHelper.addEventToGoogleCalendar(
                            event.getSummary(),
                            event.getDescription(),
                            new Date(event.getStart().getDateTime().getValue()),
                            (int) ((event.getEnd().getDateTime().getValue() - event.getStart().getDateTime().getValue()) / 60000)
                    );

                    if (addedEvent != null && addedEvent.getId() != null) {
                        Log.d(TAG, "✅ Wydarzenie dodane: " + addedEvent.getHtmlLink());
                        return "Wydarzenie dodane: " + addedEvent.getHtmlLink();
                    } else {
                        Log.e(TAG, "❌ Błąd dodawania wydarzenia.");
                        return "Błąd dodawania wydarzenia";
                    }

                } catch (UserRecoverableAuthIOException e) {
                    error = e; // Zapisujemy wyjątek do obsługi w onPostExecute()
                    return "Wymagana autoryzacja Google";
                } catch (Exception e) {
                    Log.e(TAG, "❌ Wystąpił błąd podczas dodawania wydarzenia", e);
                    return "Błąd: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (error instanceof UserRecoverableAuthIOException) {
                    Log.e(TAG, "⚠ Wymagana zgoda użytkownika na dostęp do kalendarza.");
                    UserRecoverableAuthIOException authException = (UserRecoverableAuthIOException) error;
                    Intent consentIntent = authException.getIntent();
                    ((Activity) RecipesActivity.this).startActivityForResult(consentIntent, REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(RecipesActivity.this, result, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Wynik dodawania wydarzenia: " + result);
                }
            }
        }.execute();
    }


    private void showDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                for (Recipe recipe : adapter.getSelectedRecipes()) {
                    addEventToGoogleCalendar(recipe.getName(), "Przepis z CookMate: " + recipe.getDescription(), calendar.getTime(), recipe.getPreparationTime());
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private final ActivityResultLauncher<Intent> accountPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String accountName = result.getData().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        getPreferences(MODE_PRIVATE).edit().putString(PREF_ACCOUNT_NAME, accountName).apply();
                        Log.d("RecipesActivity", "Wybrano konto Google: " + accountName);
                        Toast.makeText(this, "Wybrano konto: " + accountName, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("RecipesActivity", "Nie udało się pobrać nazwy konta.");
                    }
                } else {
                    Log.e("RecipesActivity", "Użytkownik anulował wybór konta.");
                    Toast.makeText(this, "Musisz wybrać konto Google!", Toast.LENGTH_LONG).show();
                }
            });

    private void chooseGoogleAccount() {
        Intent intent = credential.newChooseAccountIntent();
        accountPickerLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Uprawnienia przyznane!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Brak wymaganych uprawnień do kalendarza!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestCalendarPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR
            }, 200);
        }
    }
}