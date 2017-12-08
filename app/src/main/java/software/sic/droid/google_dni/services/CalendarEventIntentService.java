/*
 * www.sic-software.com
 * google-dni_smart_news
 *
 * @date Feb 2017
 */
package software.sic.droid.google_dni.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.data.CalendarEvent;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

/**
 * Class to get the events of the current day
 */
public class CalendarEventIntentService extends IntentService {
    // Log tag
    private static final String TAG = "CalEventService";
    // Interval for update requests (5 minutes)
    private static final long INTERVAL = 5 * 60 * 1000;
    // Constant for the calendar events pre check
    private static final int MINUTES_TO_BE_ADDED = 5;

    public CalendarEventIntentService() {
        super(TAG);
    }//c'tor

    /**
     * Sets the timer for the interval in which this service should be called and registers the service to the alarm
     * manager
     *
     * @param context The context in which the service is created
     */
    public static void queueTimer(@NonNull Context context) {
        // Get the systems alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Create the pending intent to receive the broadcast
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(CalendarEventIntentService.class
                .getCanonicalName(), null, context, MyApplication.AlarmReceiver.class), 0);
        // Hand over the pending intent to the alarm manger and set the interval
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + INTERVAL,
                INTERVAL, pendingIntent);
    }//queueTimer

    /**
     * Starts the service
     *
     * @param context Context in which the service is running
     */
    public static void startService(@NonNull Context context) {
        // Create the intent for the intent service
        Intent intent = new Intent(context, CalendarEventIntentService.class);
        // Pass the intent service to the wakeful service
        WakefulBroadcastReceiver.startWakefulService(context, intent);
    }//startService

    /**
     * Gets all calendar events for the current day. This also includes events ending that day or starting that day
     * as well as events that start before that day and end after that day.
     *
     * @param context The context
     * @return A list of all the calendar events for the current day
     */
    @Nullable
    private static ArrayList<CalendarEvent> getCurrentCalendarEvents(@NonNull Context context) {
        // Create array list to store all calendar events
        ArrayList<CalendarEvent> calendarEvents;
        // Get date of today
        Calendar startOfToday = new GregorianCalendar();
        startOfToday.set(Calendar.HOUR_OF_DAY, 0);
        startOfToday.set(Calendar.MINUTE, 0);
        startOfToday.set(Calendar.SECOND, 0);
        // Get date of tomorrow by adding one day to the current day
        Calendar endOfToday = new GregorianCalendar();
        endOfToday.add(Calendar.DAY_OF_MONTH, 1);
        endOfToday.set(Calendar.HOUR_OF_DAY, 0);
        endOfToday.set(Calendar.MINUTE, 0);
        endOfToday.set(Calendar.SECOND, 0);
        // Build the uri from the android calendar
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        // Append start and end dates to the uri to filter for events occurring on the present day
        ContentUris.appendId(eventsUriBuilder, startOfToday.getTimeInMillis());
        ContentUris.appendId(eventsUriBuilder, endOfToday.getTimeInMillis());
        // Build the uri
        Uri eventsUri = eventsUriBuilder.build();
        // Create projection array with all fields requested for all events
        String[] projection = new String[]{BaseColumns._ID, CalendarContract.Events.DTSTART, CalendarContract.Events
                .DTEND, CalendarContract.Events.ALL_DAY};
        // Get the cursor for the events if we got the rights to read from the calendar
        if (ContextCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.READ_CALENDAR) ==
                PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = context.getContentResolver().query(eventsUri, projection, null, null, null);
            // Check if the cursor is null or empty
            if (cursor != null && cursor.getCount() > 0) {
                // Initialize the array for the calendar events
                calendarEvents = new ArrayList<>();
                final String userId = SmartNewsSharedPreferences.instance().getUserId();

                // Iterate over the cursor
                while (cursor.moveToNext()) {
                    try {
                        // Get all values needed from the cursor
                        long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                        Date eventStartDate = adjustToTimeZone(new Date(cursor.getLong(cursor
                                .getColumnIndex(CalendarContract.Events.DTSTART))));
                        Date eventEndDate = adjustToTimeZone(new Date(cursor.getLong(cursor
                                .getColumnIndex(CalendarContract.Events.DTEND))));
                        boolean isEventAllDay = cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.ALL_DAY)) == 1;
                        // Create new calendar event for the given entry and save it
                        calendarEvents.add(new CalendarEvent(userId, id, eventStartDate.getTime(), eventEndDate.getTime()
                                , isEventAllDay));
                    }catch(Throwable e){//see #24601 -  NumberFormatException in CalendarEventIntentService
                        if(DEBUG){Log.w(TAG,"ignore calendar entry",e);}
                    }
                }
                // Close the cursor
                cursor.close();
                // Return the calendar events
                return calendarEvents;
            }
        }
        // One of the conditions above do not match so return null
        return null;
    }//getCurrentCalendarEvents

    /**
     * Add the appropriate timezone offset for Germany to a date
     *
     * @param date Date to be converted
     */
    private static Date adjustToTimeZone(Date date) {
        //Calendar calendar = Calendar.getInstance(Locale.getDefault());
        // Get the calendar for germany
        Calendar calendar = Calendar.getInstance(Locale.GERMANY);
        // Set the offset for the calendar timezone
        date.setTime(date.getTime() - (long) (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)));
        // Return the formatted date
        return date;
    }//adjustToTimeZone

    /**
     * TODO: 05.04.17 - dg: We could use this instead of reading the calendar events from the database.
     *
     * Checks if there is an actual calendar event running right now or in some minutes (MINUTES_TO_BE_ADDED)
     *
     * @return Boolean flag if there is an calendar event running right now or not
     */
    @SuppressWarnings("unused")
    public static boolean isCalendarEventActive(Context context) {
        // Flag which stores if an calendar event is active
        boolean isCalendarEventActive = false;
        // Get the calendar events for the active day
        ArrayList<CalendarEvent> calendarEvents = CalendarEventIntentService.getCurrentCalendarEvents(context);
        // Get a instance of the calendar for the current time
        Calendar calendar = Calendar.getInstance();
        // Get another instance of the calendar for the future time
        Calendar futureCalendar = Calendar.getInstance();
        // Add some time to the calendar
        futureCalendar.add(Calendar.MINUTE, MINUTES_TO_BE_ADDED);

        // Check if calendar entries are set for the current day
        if (calendarEvents != null && calendarEvents.size() > 0) {

            // There are calendar events for today so we have to check every single one
            for (CalendarEvent calendarEvent : calendarEvents) {

                // Check if the calendar event is an all day event skip it when it is
                if (calendarEvent.getIsAllDay()) continue;

                // Check if the calendar event is overlapping with one of the given times
                if ((futureCalendar.after(calendarEvent.getStartDate()) && futureCalendar.before(calendarEvent
                        .getEndDate())) || (calendar.before(calendarEvent.getEndDate()) && calendar.after
                        (calendarEvent.getStartDate()))) {
                    // Current time or future time is during an calendar event
                    isCalendarEventActive = true;
                }
            }
        }

        // Return if there are calendar events right now or not
        return isCalendarEventActive;
    }//isCalendarEventActive

    /**
     * Creates a new calendar event entry in the database or updates it if it already exists in the database
     *
     * @param calendarEvents The calendar events to be stored in the database
     */
    private static void createOrUpdateCalendarEventsInDatabase(@Nullable ArrayList<CalendarEvent> calendarEvents) {
        if (calendarEvents == null || calendarEvents.size() == 0) return;
        // Get the database
        final MyApplication myApp = MyApplication.instance();
        Database database = myApp.acquireDatabase();
        // Save all entries in the database
        for (CalendarEvent calendarEvent : calendarEvents) {
            database.createCalendarEvent(calendarEvent);
        }
        // Close the database after all action are completed to prevent leaks
        myApp.releaseDatabase();
    }//createOrUpdateCalendarEventsInDatabase

    public static void updateCalendarDatabase(Context context) {
        createOrUpdateCalendarEventsInDatabase(getCurrentCalendarEvents(context));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Store the events in the database
        createOrUpdateCalendarEventsInDatabase(getCurrentCalendarEvents(this));
        // Complete the wakeful intent
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }//onHandleIntent
}//CalendarEventIntentService
