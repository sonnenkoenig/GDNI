/*
 * www.sic.software
 *
 * @file Database.java
 * @date 2017-01-25
 * @brief Database of the app used to store the information collected
 */
package software.sic.droid.google_dni.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.DetectedActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import software.sic.droid.google_dni.data.data.CalendarEvent;
import software.sic.droid.google_dni.data.data.DatabaseContract;
import software.sic.droid.google_dni.data.data.DbEntryNotFoundException;
import software.sic.droid.google_dni.data.data.LogEntry;
import software.sic.droid.google_dni.data.data.PendingLogEntry;
import software.sic.droid.google_dni.data.data.SensorStatus;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.PendingNewsEvent;
import software.sic.droid.google_dni.data.event.UiEvent;
import software.sic.droid.google_dni.services.ActivityIntentService;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

/**
 * Database of the app used to store the information collected
 */
public class Database extends SQLiteOpenHelper {
    // Database name and version
    private static final String DATABASE_NAME = "smart_news.db";
    private static final int DATABASE_VERSION = 17;

    // Writable database
    private final SQLiteDatabase mWritableDatabase;
    // Date format in which the date ist represented in the database
    public final static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
    // Shared preferences to access user information
    private final SmartNewsSharedPreferences mSmartNewsSharedPreferences;

    @SuppressLint("SetWorldReadable")
    private Database(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mWritableDatabase = getWritableDatabase();
        this.mSmartNewsSharedPreferences = SmartNewsSharedPreferences.instance();
        if (DEBUG) {
            File p = context.getDatabasePath(DATABASE_NAME);
            //noinspection ResultOfMethodCallIgnored
            p.setReadable(true, false);
            // Hack so we can get DB with 'adb pull /data/data/software.sic.droid.smart_news/databases/smart_news.db'
        }
    }//c'tor

    //must only by called from MyApplication
    public static Database doCreate(@NonNull Context context) {
        return new Database(context);
    }//doCreate

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseContract.ActivitiesTable.TABLE_CREATE);
        db.execSQL(DatabaseContract.CalendarEventsTable.TABLE_CREATE);
        db.execSQL(DatabaseContract.DisplayTable.TABLE_CREATE);
        db.execSQL(DatabaseContract.InterruptionFiltersTable.TABLE_CREATE);
        db.execSQL(DatabaseContract.NetworkTable.TABLE_CREATE);
        db.execSQL(DatabaseContract.NewsTable.TABLE_CREATE);
        db.execSQL(DatabaseContract.PendingNewsTable.TABLE_CREATE);
        db.execSQL(DatabaseContract.PendingLogEventsTable.TABLE_CREATE);
        db.execSQL(DatabaseContract.LogEntryTable.TABLE_CREATE);
    }//onCreate

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.ActivitiesTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.CalendarEventsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.DisplayTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.InterruptionFiltersTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.NetworkTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.NewsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.PendingNewsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.PendingLogEventsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.LogEntryTable.TABLE_NAME);
        onCreate(db);
    }//onUpgrade

    /**
     * Writes all detected activities in the given list into the database
     *
     * @param detectedActivities List of detected activities can hold one or more detected activities
     */
    public synchronized void addActivityState(@NonNull List<DetectedActivity> detectedActivities) {
        // Get the detected activities as content values
        ContentValues contentValues = this.detectedActivitiesToContentValues(detectedActivities);
        // Create the activity entry in the database
        this.mWritableDatabase.insert(DatabaseContract.ActivitiesTable.TABLE_NAME, null, contentValues);
    }//addActivityState

    /**
     * Creates content values for the detected activities
     *
     * @param detectedActivities Array list of detected activities to be transformed into content values
     * @return The content values containing the detected activities
     */
    @NonNull
    private ContentValues detectedActivitiesToContentValues(@NonNull List<DetectedActivity> detectedActivities) {
        // Get the current date and time
        Date now = new Date();
        // Generate content values for the activity
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.ActivitiesTable._TIMESTAMP, now.getTime());
        contentValues.put(DatabaseContract.ActivitiesTable._TIMESTAMP_AS_STRING, sSimpleDateFormat.format(now));
        contentValues.put(DatabaseContract.ActivitiesTable._USER_UUID, this.mSmartNewsSharedPreferences.getUserId());
        // For every detected activity in the list get the type and write the confidence of it in the database
        for (DetectedActivity detectedActivity : detectedActivities) {
            String activity_column;
            switch (detectedActivity.getType()) {
                case DetectedActivity.IN_VEHICLE:
                    activity_column = DatabaseContract.ActivitiesTable._IN_VEHICLE_CONFIDENCE;
                    break;
                case DetectedActivity.ON_BICYCLE:
                    activity_column = DatabaseContract.ActivitiesTable._ON_BICYCLE_CONFIDENCE;
                    break;
                case DetectedActivity.ON_FOOT:
                    activity_column = DatabaseContract.ActivitiesTable._ON_FOOT_CONFIDENCE;
                    break;
                case DetectedActivity.RUNNING:
                    activity_column = DatabaseContract.ActivitiesTable._RUNNING_CONFIDENCE;
                    break;
                case DetectedActivity.STILL:
                    activity_column = DatabaseContract.ActivitiesTable._STILL_CONFIDENCE;
                    break;
                case DetectedActivity.TILTING:
                    activity_column = DatabaseContract.ActivitiesTable._TILTING_CONFIDENCE;
                    break;
                case DetectedActivity.UNKNOWN:
                    activity_column = DatabaseContract.ActivitiesTable._UNKNOWN_CONFIDENCE;
                    break;
                case DetectedActivity.WALKING:
                    activity_column = DatabaseContract.ActivitiesTable._WALKING_CONFIDENCE;
                    break;
                default:
                    continue;
            }
            final int confidence = detectedActivity.getConfidence();
            contentValues.put(activity_column, confidence);
        }
        // Return the content values
        return contentValues;
    }//detectedActivitiesToContentValues

    /**
     * Writes a new calendar event in the database or updates it if it already exists
     *
     * @param calendarEvent The calendar event to be stored in the database
     */
    public synchronized void createCalendarEvent(@NonNull CalendarEvent calendarEvent) {
        // Content values for the calendar event
        ContentValues contentValues = calendarEventToContentValues(calendarEvent);
        // Where clause for the query
        String whereClause = DatabaseContract.CalendarEventsTable._ID + "=?"
                + " AND " + DatabaseContract.CalendarEventsTable._USER_UUID + "=?";
        // Arguments for the where clause
        String[] whereArgs = new String[]{Long.toString(calendarEvent.getId()), calendarEvent.getUserID()};
        // Query to get all entries where the id matches the calendarEvent id
        Cursor cursor = this.mWritableDatabase.query(DatabaseContract.CalendarEventsTable.TABLE_NAME,
                null, whereClause, whereArgs, null,
                null, null);
        // If the id already exist within the database update the entry otherwise create it
        if (cursor.moveToFirst()) {
            this.mWritableDatabase.update(DatabaseContract.CalendarEventsTable.TABLE_NAME,
                    contentValues, whereClause, whereArgs);
        } else {
            this.mWritableDatabase.insert(DatabaseContract.CalendarEventsTable.TABLE_NAME, null,
                    contentValues);
        }
        // Close the cursor
        cursor.close();
    }//createCalendarEvent

    /**
     * Creates content values from the given calendar event
     *
     * @param calendarEvent The calendar event ot be transformed into content values
     * @return The content values of the calendar event
     */
    @NonNull
    private ContentValues calendarEventToContentValues(@NonNull CalendarEvent calendarEvent) {
        // Get the current date and time
        Date now = new Date();
        // Generate content values for the calendar event
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.CalendarEventsTable._TIMESTAMP, now.getTime());
        contentValues.put(DatabaseContract.CalendarEventsTable._TIMESTAMP_AS_STRING, sSimpleDateFormat.format
                (now));
        contentValues.put(DatabaseContract.CalendarEventsTable._USER_UUID, calendarEvent.getUserID());
        contentValues.put(DatabaseContract.CalendarEventsTable._ID, calendarEvent.getId());
        contentValues.put(DatabaseContract.CalendarEventsTable._START_DATE, calendarEvent.getStartDate());
        contentValues.put(DatabaseContract.CalendarEventsTable._END_DATE, calendarEvent.getEndDate());
        contentValues.put(DatabaseContract.CalendarEventsTable._IS_ALL_DAY, calendarEvent.getIsAllDay());
        // Return the content values
        return contentValues;
    }//calendarEventToContentValues

    /**
     * Writes the given display state into the database
     *
     * @param displayState The display state to be stored in the database
     */
    public synchronized void createDisplayState(boolean displayState) {
        // Get the content values for the display state entry
        ContentValues contentValues = this.displayStateToContentValues(displayState);
        // Create the display state entry in the database
        this.mWritableDatabase.insert(DatabaseContract.DisplayTable.TABLE_NAME, null, contentValues);
    }//createDisplayState

    /**
     * Generates the content values for the display state
     *
     * @param displayState The display state to be stored in the database
     * @return The content values for the database entry
     */
    private ContentValues displayStateToContentValues(boolean displayState) {
        // Get the current date and time
        Date now = new Date();
        // Generate content values for the display state
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.DisplayTable._TIMESTAMP, now.getTime());
        contentValues.put(DatabaseContract.DisplayTable._TIMESTAMP_AS_STRING, sSimpleDateFormat.format(now));
        contentValues.put(DatabaseContract.DisplayTable._USER_UUID, this.mSmartNewsSharedPreferences.getUserId());
        contentValues.put(DatabaseContract.DisplayTable._STATE, displayState);
        // Return the content values
        return contentValues;
    }//displayStateToContentValues

    /**
     * Writes the interruption filter in the database
     *
     * @param interruptionFilter the interruption filter in form of an integer
     */
    public synchronized void createInterruptionFilter(int interruptionFilter) {
        // Get the content values for the interruption filter
        ContentValues contentValues = this.interruptionFilterToContentValues(interruptionFilter);
        // Create the interruption filter entry in the database
        this.mWritableDatabase.insert(DatabaseContract.InterruptionFiltersTable.TABLE_NAME,
                null, contentValues);
    }//createInterruptionFilter

    /**
     * Creates the content values for the interruption filter
     *
     * @param interruptionFilter The interruption filter to be transformed into content values
     * @return The content values to be stored in the database
     */
    @NonNull
    private ContentValues interruptionFilterToContentValues(int interruptionFilter) {
        // Get the current date and time
        Date now = new Date();
        // Generate content values for the interruption filter
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.InterruptionFiltersTable._TIMESTAMP, now.getTime());
        contentValues.put(DatabaseContract.InterruptionFiltersTable._TIMESTAMP_AS_STRING, sSimpleDateFormat
                .format(now));
        contentValues.put(DatabaseContract.InterruptionFiltersTable._USER_UUID, this.mSmartNewsSharedPreferences
                .getUserId());
        contentValues.put(DatabaseContract.InterruptionFiltersTable._INTERRUPTION_FILTER, interruptionFilter);
        contentValues.put(DatabaseContract.InterruptionFiltersTable._INTERRUPTION_FILTER_AS_STRING,
                interruptionFilterToString(interruptionFilter));
        // Return the content values
        return contentValues;
    }//interruptionFilterToContentValues

    /**
     * Creates an network state entry in the database with the corresponding network type if set
     *
     * @param networkStatus The network state which can be wifi or mobile data or something
     */
    public synchronized void createNetworkState(SensorStatus.NetworkStatus networkStatus) {
        // Generate the content values for the network state
        ContentValues contentValues = this.networkStateToContentValues(networkStatus);
        // Write the network state entry in the database
        this.mWritableDatabase.insert(DatabaseContract.NetworkTable.TABLE_NAME, null, contentValues);
    }//createNetworkState

    /**
     * Creates the content values for the network state entry
     *
     * @param networkStatus The network state which can be wifi or mobile data or something
     * @return Content values of the network state entry
     */
    private ContentValues networkStateToContentValues(SensorStatus.NetworkStatus networkStatus) {
        // Get the current date and time
        Date now = new Date();
        // Generate content values for the interruption filter
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.NetworkTable._TIMESTAMP, now.getTime());
        contentValues.put(DatabaseContract.NetworkTable._TIMESTAMP_AS_STRING, sSimpleDateFormat.format(now));
        contentValues.put(DatabaseContract.NetworkTable._USER_UUID, this.mSmartNewsSharedPreferences.getUserId());
        if (null != networkStatus) {
            contentValues.put(DatabaseContract.NetworkTable._STATE, networkStatus.networkState.ordinal());
            contentValues.put(DatabaseContract.NetworkTable._TYPE, networkStatus.signalStrength);
        }
        // Return the content values
        return contentValues;
    }//networkStateToContentValues

    /**
     * Create the content values for a news event
     *
     * @param newsEvent The news event to be transformed into content values
     * @return The from the news event resulting content values
     */
    @NonNull
    private ContentValues newsEventToContentValues(@NonNull NewsEvent newsEvent) {
        // Generate content values for the news event
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.NewsTable._TIMESTAMP, newsEvent.timeStamp);
        contentValues.put(DatabaseContract.NewsTable._TIMESTAMP_AS_STRING, sSimpleDateFormat.format(new Date
                (newsEvent.timeStamp)));
        contentValues.put(DatabaseContract.NewsTable._ID, newsEvent.id);
        contentValues.put(DatabaseContract.NewsTable._TITLE, newsEvent.title);
        contentValues.put(DatabaseContract.NewsTable._SUMMARY, newsEvent.summary);
        contentValues.put(DatabaseContract.NewsTable._CONTENT, newsEvent.content);
        contentValues.put(DatabaseContract.NewsTable._TYPE, newsEvent.type != null ? newsEvent.type.ordinal() : -1);
        // Return the content values
        return contentValues;
    }//newsEventToContentValues

    /**
     * Matches the given interruption filter value given as an int into a human readable String
     *
     * @param interruptionFilter The interruption filter value as int
     * @return The interruption filter readable for humans
     */
    @NonNull
    private String interruptionFilterToString(int interruptionFilter) {
        switch (interruptionFilter) {
            case 0:
                return "unknown";
            case 1:
                return "all";
            case 2:
                return "priority";
            case 3:
                return "none";
            case 4:
                return "alarms";
            default:
                return "unknown";
        }
    }//interruptionFilterToString

    public synchronized boolean addNews(@NonNull NewsEvent aNewsEvent) {
        if (!containsNewsEventById(aNewsEvent.id)) {
            ContentValues contentValues = this.newsEventToContentValues(aNewsEvent);
            // Create the news event entry in the database
            aNewsEvent.rowId = this.mWritableDatabase.insert(DatabaseContract.NewsTable.TABLE_NAME, null,
                    contentValues);

            return true;
        }

        return false;
    }//addNews

    private boolean containsNewsEventById(String id) {
        String[] projection = {
                DatabaseContract.NewsTable._ROWID,
                DatabaseContract.NewsTable._ID,
        };
        String selection = DatabaseContract.NewsTable._ID + "=?";
        String[] selectionArgs = new String[]{id};
        String orderBy = DatabaseContract.NewsTable._ROWID + " ASC";

        Cursor newsResult = this.mWritableDatabase.query(DatabaseContract.NewsTable.TABLE_NAME, projection,
                selection, selectionArgs, null, null, orderBy);
        boolean result = newsResult.moveToFirst();
        newsResult.close();

        return result;
    }

    public void addLogEntry(@NonNull UiEvent aUiEvent, @NonNull SensorStatus aSensorStatus) {
        addLogEntry(aSensorStatus, aUiEvent.timeStamp, aUiEvent.userId, aUiEvent.newsEvent.rowId, aUiEvent.action,
                aUiEvent.algorithm);
    }//addLogEntry

    public synchronized void addLogEntry(@NonNull SensorStatus aSensorStatus, long aTimeStamp, @NonNull String
            aUserId, long aNewsEventRowId, @NonNull UiEvent.Action aAction, LogEntry.Algorithm aAlgorithm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.LogEntryTable._TIMESTAMP, aTimeStamp);
        contentValues.put(DatabaseContract.LogEntryTable._TIMESTAMP_AS_STRING, sSimpleDateFormat.format(new Date
                (aTimeStamp)));
        contentValues.put(DatabaseContract.LogEntryTable._USER_UUID, aUserId);
        contentValues.put(DatabaseContract.LogEntryTable._NEWS_ROW_ID, aNewsEventRowId);
        contentValues.put(DatabaseContract.LogEntryTable._UI_ACTION, aAction.ordinal());

        contentValues.put(DatabaseContract.LogEntryTable._USED_ALGORITHM, aAlgorithm.ordinal());
        contentValues.put(DatabaseContract.LogEntryTable._SENSOR_ACTIVITY, aSensorStatus.activitiesStatus
                .detectedActivitiesState.ordinal());
        if(null!=aSensorStatus.activitiesStatus.detectedActivities) {
            contentValues.put(DatabaseContract.LogEntryTable._SENSOR_ACTIVITIES, aSensorStatus.activitiesStatus.detectedActivities.toString());
        }
        contentValues.put(DatabaseContract.LogEntryTable._INTERRUPTION_FILTER, aSensorStatus.interruptionFilterStatus
                .interruptionFilter.ordinal());
        contentValues.put(DatabaseContract.LogEntryTable._NETWORK_STATE, aSensorStatus.networkStatus.networkState
                .ordinal());
        contentValues.put(DatabaseContract.LogEntryTable._NETWORK_RSSI, aSensorStatus.networkStatus.signalStrength);
        contentValues.put(DatabaseContract.LogEntryTable._DISPLAY, aSensorStatus.displayStatus.isDisplayActive);
        contentValues.put(DatabaseContract.LogEntryTable._CAL_PREV_START, aSensorStatus.calendarStatus
                .previousStartTime);
        contentValues.put(DatabaseContract.LogEntryTable._CAL_PREV_END, aSensorStatus.calendarStatus.previousEndTime);
        contentValues.put(DatabaseContract.LogEntryTable._CAL_NEXT_START, aSensorStatus.calendarStatus.nextStartTime);
        contentValues.put(DatabaseContract.LogEntryTable._CAL_NEXT_END, aSensorStatus.calendarStatus.nextEndTime);
        contentValues.put(DatabaseContract.LogEntryTable._CAL_TODAY_ENTRIES, aSensorStatus.calendarStatus.todayEntries);
        contentValues.put(DatabaseContract.LogEntryTable._CAL_CURRENT_ENTRIES, aSensorStatus.calendarStatus
                .currentEntries);

        long rowID = this.mWritableDatabase.insert(DatabaseContract.LogEntryTable.TABLE_NAME, null, contentValues);
        if (rowID >= 0) {
            contentValues = new ContentValues();
            contentValues.put(DatabaseContract.PendingLogEventsTable._EVENT_ROW_ID, rowID);
            this.mWritableDatabase.insert(DatabaseContract.PendingLogEventsTable.TABLE_NAME, null, contentValues);
        }
    }//addLogEntry

    @Nullable
    public LogEntry getLogEntry(long aRowId) {
        LogEntry result = null;
        Cursor cursor = mWritableDatabase.rawQuery(
                "SELECT * FROM " + DatabaseContract.LogEntryTable.TABLE_NAME + " WHERE rowid=" + aRowId
                , null//String[] selectionArgs
        );
        if (cursor.moveToFirst()) {
            result = new LogEntry(cursor, mWritableDatabase);
        }
        cursor.close();

        return result;
    }//getLogEntry

    @Nullable
    public String getPendingNewsCount(String aUserId) {
        String result = null;
        Cursor cursor = mWritableDatabase.rawQuery(
                "SELECT " + DatabaseContract.PendingNewsTable._TYPE + " , count(*) "
                +" FROM " + DatabaseContract.PendingNewsTable.TABLE_NAME
                +" WHERE " + DatabaseContract.PendingNewsTable._USER_UUID + "=?"
                +" GROUP BY " + DatabaseContract.PendingNewsTable._TYPE
                , new String[]{aUserId}// selectionArgs
        );
        if (cursor.moveToFirst()) {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append(cursor.getInt(0)).append('.').append(cursor.getInt(1)).append(';');
            }while (cursor.moveToNext());
            result = sb.toString();
        }
        cursor.close();
        return result;
    }//getPendingNewsCount

    /**
     * read last reported event for aUserId and test if it has the
     *
     * @return -1 if not found or Action != UiEvent.Action.NOTIFIED
     */
    public long getReplacedNewsRowId(String aUserId) {
        long replacedNewsRowId = -1;
        Cursor cursor = mWritableDatabase.rawQuery(
                "SELECT " + DatabaseContract.LogEntryTable._UI_ACTION + " , " + DatabaseContract.LogEntryTable
                        ._NEWS_ROW_ID
                        + " FROM " + DatabaseContract.LogEntryTable.TABLE_NAME
                        + " WHERE " + DatabaseContract.LogEntryTable._USER_UUID + " =?"
                        + " AND " + DatabaseContract.LogEntryTable._UI_ACTION+ " !="+UiEvent.Action.RECEIVED.ordinal()
                        + " ORDER BY rowid DESC"
                        + " limit 1"
                , new String[]{aUserId}// selectionArgs
        );
        if (cursor.moveToFirst()) {
            if (UiEvent.Action.NOTIFIED.ordinal() == cursor.getInt(0)) {
                replacedNewsRowId = cursor.getInt(1);
            }
        }
        cursor.close();
        return replacedNewsRowId;
    }//getReplacedNewsRowId

    /**
     * Returns the latest NewsEntry which hasn't been shown yet.
     *
     * @param aUserId The User Id for which news should be returned
     * @param aType   The type of the news to be retrieved, put null here for any type
     * @return the latest {@link PendingNewsEvent}, null if no entries exist
     */
    @Nullable
    public PendingNewsEvent getLatestPendingNewsEvent(@NonNull String aUserId, @Nullable NewsEvent.Type aType) {
        PendingNewsEvent latestPendingNewsEvent;
        String selection;
        String[] selectionArgs;

        String[] projection = {
                DatabaseContract.PendingNewsTable._ROWID,
                DatabaseContract.PendingNewsTable._USER_UUID,
                DatabaseContract.PendingNewsTable._NEWS_ROW_ID,
                DatabaseContract.PendingNewsTable._TYPE
        };

        if (aType == null) {
            selection = DatabaseContract.PendingNewsTable._USER_UUID + "=?";
            selectionArgs = new String[]{aUserId};

        } else {
            selection = DatabaseContract.PendingNewsTable._USER_UUID + "=? AND " +
                    DatabaseContract.PendingNewsTable._TYPE + "=? ";
            selectionArgs = new String[]{aUserId, Integer.toString(aType.ordinal())};
        }

        try (Cursor pendingNewsResult = this.mWritableDatabase.query(DatabaseContract.PendingNewsTable
                .TABLE_NAME, projection, selection, selectionArgs, null, null, null)) {
            latestPendingNewsEvent = pendingNewsEventFromCursor(pendingNewsResult);
        } catch (DbEntryNotFoundException ignore) {
            return null;
        }

        return latestPendingNewsEvent;
    }//getLatestPendingNewsEntry

    /**
     * Converts a row received from a database query into a {@link PendingNewsEvent} object.
     *
     * @param queryResult The cursor containing the row to be converted
     * @return the converted {@link PendingNewsEvent}
     * @throws DbEntryNotFoundException Thrown when the Cursor is empty
     */
    private PendingNewsEvent pendingNewsEventFromCursor(Cursor queryResult) throws DbEntryNotFoundException {

        if (queryResult.moveToFirst()) {
            return new PendingNewsEvent(
                    queryResult.getLong(queryResult.getColumnIndex(DatabaseContract.PendingNewsTable._ROWID)),
                    queryResult.getString(queryResult.getColumnIndex(DatabaseContract.PendingNewsTable
                            ._USER_UUID)),
                    queryResult.getLong(queryResult.getColumnIndex(DatabaseContract.PendingNewsTable._NEWS_ROW_ID)),
                    queryResult.getInt(queryResult.getColumnIndex(DatabaseContract.PendingNewsTable._TYPE))
            );
        } else {
            throw new DbEntryNotFoundException();
        }

    }//newsEventFromCursor

    @Nullable
    public NewsEvent getNewsEvent(long newsRowId) {

        NewsEvent newsEvent;

        String[] projection = {
                DatabaseContract.NewsTable._ROWID,
                DatabaseContract.NewsTable._TIMESTAMP,
                DatabaseContract.NewsTable._TIMESTAMP_AS_STRING,
                DatabaseContract.NewsTable._ID,
                DatabaseContract.NewsTable._TITLE,
                DatabaseContract.NewsTable._SUMMARY,
                DatabaseContract.NewsTable._CONTENT,
                DatabaseContract.NewsTable._TYPE
        };
        String selection = DatabaseContract.NewsTable._ROWID + "=?";
        String[] selectionArgs = new String[]{Long.toString(newsRowId)};
        String orderBy = DatabaseContract.NewsTable._ROWID + " ASC";

        try (Cursor newsResult = this.mWritableDatabase.query(DatabaseContract.NewsTable.TABLE_NAME, projection,
                selection, selectionArgs, null, null, orderBy)) {
            newsEvent = newsEventFromCursor(newsResult);
        } catch (DbEntryNotFoundException ignore) {
            //e.printStackTrace();
            return null;
        }

        return newsEvent;
    }//getNewsEvent

    /**
     * Returns the latest NewsEntry in the database.
     *
     * @param aType The type of the news to be retrieved, put null here for any type
     * @return the latest {@link PendingNewsEvent}, null if no entries exist
     */
    @Nullable
    public NewsEvent getLatestNewsEvent(@Nullable NewsEvent.Type aType) {
        NewsEvent latestNewsEvent;
        String selection = null;
        String[] selectionArgs = null;
        String orderBy = DatabaseContract.NewsTable._ROWID + " DESC";

        String[] projection = {
                DatabaseContract.NewsTable._ROWID,
                DatabaseContract.NewsTable._TIMESTAMP,
                DatabaseContract.NewsTable._TIMESTAMP_AS_STRING,
                DatabaseContract.NewsTable._ID,
                DatabaseContract.NewsTable._TITLE,
                DatabaseContract.NewsTable._SUMMARY,
                DatabaseContract.NewsTable._CONTENT,
                DatabaseContract.NewsTable._TYPE
        };

        if (aType != null) {
            selection = DatabaseContract.NewsTable._TYPE + "=? ";
            selectionArgs = new String[]{Integer.toString(aType.ordinal())};
        }
//        if(DEBUG){
//            selection = DatabaseContract.NewsTable._TYPE + "='xyz' ";
//        }

        try (Cursor newsResult = this.mWritableDatabase.query(DatabaseContract.NewsTable
                .TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy)) {
            latestNewsEvent = newsEventFromCursor(newsResult);
        } catch (DbEntryNotFoundException ignore) {
            return null;
        }

        return latestNewsEvent;
    }

    /**
     * Converts a row received from a database query into a {@link NewsEvent} object.
     *
     * @param queryResult The cursor containing the row to be converted
     * @return the converted {@link NewsEvent} object
     * @throws DbEntryNotFoundException Thrown when the Cursor is empty
     */
    @NonNull
    private NewsEvent newsEventFromCursor(Cursor queryResult) throws DbEntryNotFoundException {

        if (queryResult.moveToFirst()) {
            return new NewsEvent(
                    queryResult.getLong(queryResult.getColumnIndex(DatabaseContract.NewsTable._ROWID)),
                    queryResult.getLong(queryResult.getColumnIndex(DatabaseContract.NewsTable._TIMESTAMP)),
                    queryResult.getString(queryResult.getColumnIndex(DatabaseContract.NewsTable._ID)),
                    queryResult.getString(queryResult.getColumnIndex(DatabaseContract.NewsTable._TITLE)),
                    queryResult.getString(queryResult.getColumnIndex(DatabaseContract.NewsTable._SUMMARY)),
                    queryResult.getString(queryResult.getColumnIndex(DatabaseContract.NewsTable._CONTENT)),
                    queryResult.getInt(queryResult.getColumnIndex(DatabaseContract.NewsTable._TYPE))
            );
        } else {
            throw new DbEntryNotFoundException();
        }
    }//newsEventFromCursor

    public synchronized void deletePendingNewsEvent(long pendingNewsRowId) throws DbEntryNotFoundException {
        int result;
        String selection = DatabaseContract.PendingNewsTable._ROWID + "=?";
        String[] selectionArgs = new String[]{Long.toString(pendingNewsRowId)};

        result = this.mWritableDatabase.delete(
                DatabaseContract.PendingNewsTable.TABLE_NAME,
                selection,
                selectionArgs
        );

        if (result == 0) {
            throw new DbEntryNotFoundException(pendingNewsRowId);
        }
    }//deletePendingNewsEvent

    /**
     * Gets the status of the users calendar on the specified date
     * (Status includes closest Events to the specified Time and total
     * number of events on the specified date)
     *
     * @param aTime   The date for which the status should be retrieved
     * @param aUserId The User id for which the status should be retrieved
     * @return The status
     */
    @NonNull
    public SensorStatus.CalendarStatus getCalendarStatus(long aTime, String aUserId) {

        SensorStatus.CalendarStatus calendarStatus = new SensorStatus.CalendarStatus();

        CalendarEvent previousEvent = getClosestCalendarEvent(aUserId, aTime, false);
        CalendarEvent nextEvent = getClosestCalendarEvent(aUserId, aTime, true);

        if (previousEvent != null) {
            calendarStatus.previousStartTime = previousEvent.getStartDate();
            calendarStatus.previousEndTime = previousEvent.getEndDate();
        } else {
            calendarStatus.previousStartTime = -1;
            calendarStatus.previousEndTime = -1;
        }

        if (nextEvent != null) {
            calendarStatus.nextStartTime = nextEvent.getStartDate();
            calendarStatus.nextEndTime = nextEvent.getEndDate();
        } else {
            calendarStatus.nextStartTime = -1;
            calendarStatus.nextEndTime = -1;
        }

        calendarStatus.currentEntries = getNumberOfCurrentEvents(aUserId, aTime);

        calendarStatus.todayEntries = getNumberOfEvents(aUserId, aTime);
        return calendarStatus;
    }//getCalendarStatus

    /**
     * Gets the number of Events that are occurring at the specified time
     *
     * @param aUserId The User id for which events should be found
     * @param aTime   The date for which events are searched
     * @return The number of events
     */
    private int getNumberOfCurrentEvents(@NonNull String aUserId, long aTime) {
        int count;

        //filter all-day events
        String selection = DatabaseContract.CalendarEventsTable._START_DATE + "<=? AND " +
                DatabaseContract.CalendarEventsTable._END_DATE + ">=? AND " +
                DatabaseContract.CalendarEventsTable._IS_ALL_DAY + "=? AND " +
                DatabaseContract.CalendarEventsTable._USER_UUID + "=?";
        String[] selectionArgs = new String[]{
                Long.toString(aTime),
                Long.toString(aTime),
                "0",
                aUserId
        };
        String orderBy = DatabaseContract.CalendarEventsTable._START_DATE + " ASC";

        Cursor calendarEventResult = this.mWritableDatabase.query(DatabaseContract.CalendarEventsTable
                .TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);
        count = calendarEventResult.getCount();
        calendarEventResult.close();
        // Return the count
        return count;
    }//getNumberOfCurrentEvents

    /**
     * Gets the number of Events that occur in a calendar on the specified date
     *
     * @param aUserId The User id for which events should be found
     * @param aTime   The origin date from which the search is started
     * @return The number of events
     */
    private int getNumberOfEvents(@NonNull String aUserId, long aTime) {
        int count;
        Calendar calendarDayStart = new GregorianCalendar();
        calendarDayStart.setTimeInMillis(aTime);
        calendarDayStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarDayStart.set(Calendar.MINUTE, 0);
        calendarDayStart.set(Calendar.SECOND, 0);
        calendarDayStart.set(Calendar.MILLISECOND, 0);

        Calendar calendarDayEnd = (Calendar) calendarDayStart.clone();
        calendarDayEnd.add(Calendar.DAY_OF_MONTH, 1);

        String selection = DatabaseContract.CalendarEventsTable._START_DATE + "<=? AND " +
                DatabaseContract.CalendarEventsTable._END_DATE + ">=? AND " +
                DatabaseContract.CalendarEventsTable._IS_ALL_DAY + "=? AND " +
                DatabaseContract.CalendarEventsTable._USER_UUID + "=?";
        String[] selectionArgs = new String[]{
                Long.toString(calendarDayEnd.getTimeInMillis()),
                Long.toString(calendarDayStart.getTimeInMillis()),
                "0",
                aUserId
        };
        String orderBy = DatabaseContract.CalendarEventsTable._START_DATE + " ASC";

        Cursor calendarEventResult = this.mWritableDatabase.query(DatabaseContract.CalendarEventsTable
                .TABLE_NAME, null, selection, selectionArgs, null, null, orderBy);
        count = calendarEventResult.getCount();
        calendarEventResult.close();

//        if(DEBUG){Log.v(TAG,"getNumberOfEvents = "+count+" origin="+origin+"~"+origin.getTime()+"
// s="+calendarDayStart+"~"+calendarDayStart.getTimeInMillis()+" - "+calendarDayEnd+"~"+calendarDayEnd
// .getTimeInMillis());}
        return count;
    }//getNumberOfEvents

    /**
     * Gets the CalendarEvent starting closest to the specified Date from the Database.
     * For events beginning only at or after specified Time, set startInFuture to true
     * For events beginning only at or before the specified time, set startInFuture to false
     *
     * @param aUserId       The User id for which events should be found
     * @param aTime         The origin date from which the search is started
     * @param startInFuture Switch for events to be searched for
     * @return The {@link CalendarEvent} closest to the origin Date
     */
    @Nullable
    private CalendarEvent getClosestCalendarEvent(@NonNull String aUserId, long aTime, boolean startInFuture) {
        CalendarEvent closestCalendarEvent;

        String selection;
        String[] selectionArgs;
        String orderBy;

        if (startInFuture) {
            selection = DatabaseContract.CalendarEventsTable._START_DATE + ">=? AND " + DatabaseContract
                    .CalendarEventsTable._USER_UUID + "=?";
            selectionArgs = new String[]{Long.toString(aTime), aUserId};
            orderBy = DatabaseContract.CalendarEventsTable._START_DATE + " ASC";
        } else {
            selection = DatabaseContract.CalendarEventsTable._START_DATE + "<? AND " + DatabaseContract
                    .CalendarEventsTable._USER_UUID + "=?";
            selectionArgs = new String[]{Long.toString(aTime), aUserId};
            orderBy = DatabaseContract.CalendarEventsTable._END_DATE + " DESC";
        }

        try (Cursor calendarEventResult = this.mWritableDatabase.query(DatabaseContract.CalendarEventsTable
                .TABLE_NAME, null, selection, selectionArgs, null, null, orderBy)) {
            closestCalendarEvent = calendarEventFromCursor(calendarEventResult);
        } catch (DbEntryNotFoundException ignore) {
            //e.printStackTrace();
            return null;
        }
        return closestCalendarEvent;
    }//getClosestCalendarEvent

    /**
     * Converts a row received from a database query into a {@link CalendarEvent} object.
     *
     * @param queryResult The cursor containing the row to be converted
     * @return the converted {@link CalendarEvent} object
     * @throws DbEntryNotFoundException Thrown when the Cursor is empty
     */
    @NonNull
    private CalendarEvent calendarEventFromCursor(Cursor queryResult) throws DbEntryNotFoundException {
        if (queryResult.moveToFirst()) {
            return new CalendarEvent(
                    queryResult.getString(queryResult.getColumnIndex(DatabaseContract.CalendarEventsTable._USER_UUID)),
                    queryResult.getLong(queryResult.getColumnIndex(DatabaseContract.CalendarEventsTable._ID)),
                    queryResult.getLong(queryResult.getColumnIndex(DatabaseContract.CalendarEventsTable._START_DATE)),
                    queryResult.getLong(queryResult.getColumnIndex(DatabaseContract.CalendarEventsTable._END_DATE)),
                    0 != queryResult.getLong(queryResult.getColumnIndex(DatabaseContract.CalendarEventsTable
                            ._IS_ALL_DAY))
            );
        } else {
            throw new DbEntryNotFoundException();
        }
    }//calendarEventFromCursor

    public synchronized void addPendingNewsEntry(@NonNull String aUserId, @NonNull NewsEvent aNewsEvent) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseContract.PendingNewsTable._USER_UUID, aUserId);
        contentValues.put(DatabaseContract.PendingNewsTable._NEWS_ROW_ID, aNewsEvent.rowId);
        contentValues.put(DatabaseContract.PendingNewsTable._TYPE, aNewsEvent.type != null ? aNewsEvent.type.ordinal
                () : -1);
        this.mWritableDatabase.insert(DatabaseContract.PendingNewsTable.TABLE_NAME, null, contentValues);
    }//addPendingNewsEntry

    @Nullable
    public PendingLogEntry getPendingLogEntry() {
        PendingLogEntry result = null;
        //mWritableDatabase.compileStatement()
        Cursor cursor = mWritableDatabase.rawQuery(
                "SELECT rowid,* FROM " + DatabaseContract.PendingLogEventsTable.TABLE_NAME + " LIMIT 1"
                , null//String[] selectionArgs
        );
        if (cursor.moveToFirst()) {
            result = new PendingLogEntry();
            result.rowId = cursor.getLong(0);
            result.logEventRowId = cursor.getLong(1);
        }
        cursor.close();
        return result;
    }//getPendingLogEntry

    public void deletePendingLogEntry(@NonNull PendingLogEntry entry) {
        this.mWritableDatabase.delete(DatabaseContract.PendingLogEventsTable.TABLE_NAME, "rowid=" + entry.rowId, null);
    }//deleteFirstPendingLogId

    /**
     * Gets the activity status out of the database. The activity status holds all the important information used in
     * the algorithm
     *
     * @param uuid The uuid for the user
     * @return The activity status for the given user
     */
    @NonNull
    public SensorStatus.ActivitiesStatus getActivitiesStatus(final String uuid) {
        // Where clause for the database query
        String selection = DatabaseContract.ActivitiesTable._USER_UUID + "=?";
        // Arguments for the where clause
        String[] selectionArgs = new String[]{uuid};
        // Selection which sets the order structure of the results
        String orderBy = DatabaseContract.ActivitiesTable._TIMESTAMP + " DESC";
        // Limits the results
        String limit = "1";
        // Query the database operation
        Cursor cursor = this.mWritableDatabase.query(DatabaseContract.ActivitiesTable.TABLE_NAME, null, selection,
                selectionArgs, null, null, orderBy, limit);
        // Create the activities status
        SensorStatus.ActivitiesStatus activitiesStatus = new SensorStatus.ActivitiesStatus();
        if (cursor.moveToFirst()) {
            // If the cursor is not empty we set the information to the network status
            activitiesStatus.detectedActivities = cursorToDetectedActivities(cursor);
            activitiesStatus.timeStamp = cursor.getLong(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._TIMESTAMP));
            activitiesStatus.detectedActivitiesState = getHighestDetectedActivityState(activitiesStatus
                    .detectedActivities);
        } else {
            // If the cursor is empty we return the unknown status
            activitiesStatus.detectedActivities = null;
            activitiesStatus.detectedActivitiesState = ActivityIntentService.DetectedActivityState.UNKNOWN;
        }
        // Close the cursor
        cursor.close();
        // Return the network status
        return activitiesStatus;
    }//getActivitiesStatus

    /**
     * Checks which activity has the highest value
     *
     * @param detectedActivities The detected activities to be compared
     * @return The highest detected activity
     */
    @NonNull
    private ActivityIntentService.DetectedActivityState getHighestDetectedActivityState(@NonNull ActivityIntentService
            .DetectedActivities detectedActivities) {
        // Initialize the integer for the evaluation and the return value
        int highestDetectedActivity = 0;
        ActivityIntentService.DetectedActivityState highestDetectedActivityState = ActivityIntentService
                .DetectedActivityState.UNKNOWN;
        // Evaluate the highest detected activity
        if (detectedActivities.inVehicle > highestDetectedActivity) {
            highestDetectedActivity = detectedActivities.inVehicle;
            highestDetectedActivityState = ActivityIntentService.DetectedActivityState.IN_VEHICLE;
        }
        if (detectedActivities.onBicycle > highestDetectedActivity) {
            highestDetectedActivity = detectedActivities.onBicycle;
            highestDetectedActivityState = ActivityIntentService.DetectedActivityState.ON_BICYCLE;
        }
        if (detectedActivities.onFoot > highestDetectedActivity) {
            highestDetectedActivity = detectedActivities.onFoot;
            highestDetectedActivityState = ActivityIntentService.DetectedActivityState.ON_FOOT;
        }
        if (detectedActivities.running > highestDetectedActivity) {
            highestDetectedActivity = detectedActivities.running;
            highestDetectedActivityState = ActivityIntentService.DetectedActivityState.RUNNING;
        }
        if (detectedActivities.still > highestDetectedActivity) {
            highestDetectedActivity = detectedActivities.still;
            highestDetectedActivityState = ActivityIntentService.DetectedActivityState.STILL;
        }
        if (detectedActivities.tilting > highestDetectedActivity) {
            highestDetectedActivity = detectedActivities.tilting;
            highestDetectedActivityState = ActivityIntentService.DetectedActivityState.TILTING;
        }
        if (detectedActivities.unknown > highestDetectedActivity) {
            highestDetectedActivity = detectedActivities.unknown;
            highestDetectedActivityState = ActivityIntentService.DetectedActivityState.UNKNOWN;
        }
        if (detectedActivities.walking > highestDetectedActivity) {
            highestDetectedActivityState = ActivityIntentService.DetectedActivityState.WALKING;
        }
        // Return the state of the highest detected activity
        return highestDetectedActivityState;
    }//getHighestDetectedActivitiesState

    /**
     * Makes the given cursor to an detected activity
     *
     * @param cursor Cursor pointing to an detected activity in the database
     * @return The detected activity
     */
    private ActivityIntentService.DetectedActivities cursorToDetectedActivities(Cursor cursor) {
        // Create a new detected activities object
        ActivityIntentService.DetectedActivities detectedActivities = new ActivityIntentService.DetectedActivities();
        // Add the values to the detected activities to the object if non null
        if (!cursor.isNull(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._IN_VEHICLE_CONFIDENCE)))
            detectedActivities.inVehicle = cursor.getInt(cursor.getColumnIndex
                    (DatabaseContract.ActivitiesTable._IN_VEHICLE_CONFIDENCE));
        if (!cursor.isNull(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._ON_BICYCLE_CONFIDENCE)))
            detectedActivities.onBicycle = cursor.getInt(cursor.getColumnIndex
                    (DatabaseContract.ActivitiesTable._ON_BICYCLE_CONFIDENCE));
        if (!cursor.isNull(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._ON_FOOT_CONFIDENCE)))
            detectedActivities.onFoot = cursor.getInt(cursor.getColumnIndex
                    (DatabaseContract.ActivitiesTable._ON_FOOT_CONFIDENCE));
        if (!cursor.isNull(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._RUNNING_CONFIDENCE)))
            detectedActivities.running = cursor.getInt(cursor.getColumnIndex
                    (DatabaseContract.ActivitiesTable._RUNNING_CONFIDENCE));
        if (!cursor.isNull(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._STILL_CONFIDENCE)))
            detectedActivities.still = cursor.getInt(cursor.getColumnIndex
                    (DatabaseContract.ActivitiesTable._STILL_CONFIDENCE));
        if (!cursor.isNull(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._TILTING_CONFIDENCE)))
            detectedActivities.tilting = cursor.getInt(cursor.getColumnIndex
                    (DatabaseContract.ActivitiesTable._TILTING_CONFIDENCE));
        if (!cursor.isNull(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._UNKNOWN_CONFIDENCE)))
            detectedActivities.unknown = cursor.getInt(cursor.getColumnIndex
                    (DatabaseContract.ActivitiesTable._UNKNOWN_CONFIDENCE));
        if (!cursor.isNull(cursor.getColumnIndex(DatabaseContract.ActivitiesTable._WALKING_CONFIDENCE)))
            detectedActivities.walking = cursor.getInt(cursor.getColumnIndex
                    (DatabaseContract.ActivitiesTable._WALKING_CONFIDENCE));
        // Return the detected activities
        return detectedActivities;
    }//cursorToDetectedActivities

}//class Database
//#############################################################################
//eof Database.java