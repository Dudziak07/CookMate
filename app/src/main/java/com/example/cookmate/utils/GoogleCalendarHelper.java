package com.example.cookmate.utils;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.widget.Toast;

import java.util.Calendar;

public class GoogleCalendarHelper {

    public static void addEventToGoogleCalendar(Context context, String title, String description, int durationMinutes) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.add(Calendar.MINUTE, 5);  // Rozpoczęcie za 5 minut
        long startMillis = beginTime.getTimeInMillis();

        Calendar endTime = (Calendar) beginTime.clone();
        endTime.add(Calendar.MINUTE, durationMinutes);
        long endMillis = endTime.getTimeInMillis();

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, description)
                .putExtra(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Błąd: Nie można dodać wydarzenia do kalendarza", Toast.LENGTH_SHORT).show();
        }
    }
}