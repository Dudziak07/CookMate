package com.example.cookmate.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.CalendarContract;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GoogleCalendarHelper {
    private static final String TAG = "GoogleCalendarHelper";
    private static final String APPLICATION_NAME = "CookMate";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CALENDAR_ID = "primary"; // Domyślny kalendarz użytkownika

    private Context context;
    private Calendar service;
    private GoogleAccountCredential credential;

    public GoogleCalendarHelper(Context context, GoogleAccountCredential credential) {
        this.context = context;
        this.credential = credential; // Przypisanie credential
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        service = new Calendar.Builder(transport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public Event addEventToGoogleCalendar(String title, String description, Date startTime, int durationMinutes) throws IOException, UserRecoverableAuthIOException {
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

            Log.d("GoogleCalendarHelper", "🔹 Tworzenie wydarzenia: " + event.toString());

            Event addedEvent = service.events().insert("primary", event).execute();

            return addedEvent; // <-- DODAJ TEN RETURN, JEŚLI GO NIE MA
        } catch (Exception e) {
            Log.e("GoogleCalendarHelper", "❌ Błąd dodawania wydarzenia", e);
            return null;
        }
    }

    private void forceCalendarSync(String accountName) {
        if (accountName == null) {
            Log.e(TAG, "Nie można wymusić synchronizacji: brak nazwy konta");
            return;
        }

        Account account = new Account(accountName, "com.google");
        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

        Log.d(TAG, "Wymuszanie synchronizacji kalendarza dla konta: " + accountName);
        ContentResolver.requestSync(account, CalendarContract.AUTHORITY, extras);
    }
}