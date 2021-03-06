package software.sic.droid.google_dni.data.data;

import android.provider.BaseColumns;


/**
 * www.sic.software
 *
 * 17.03.17
 */

public class DatabaseContract {
    interface ActivityColumns {
        String _IN_VEHICLE_CONFIDENCE = "in_vehicle_confidence";
        String _ON_BICYCLE_CONFIDENCE = "on_bicycle_confidence";
        String _ON_FOOT_CONFIDENCE = "on_foot_confidence";
        String _RUNNING_CONFIDENCE = "running_confidence";
        String _STILL_CONFIDENCE = "still_confidence";
        String _TILTING_CONFIDENCE = "tilting_confidence";
        String _UNKNOWN_CONFIDENCE = "unknown_confidence";
        String _WALKING_CONFIDENCE = "walking_confidence";
    }

    interface CalendarColumns {
        String _START_DATE = "start_date";
        String _END_DATE = "end_date";
        String _IS_ALL_DAY = "is_all_day";
    }

    interface InterruptionFilterColumns {
        String _INTERRUPTION_FILTER = "interruption_filter";
        String _INTERRUPTION_FILTER_AS_STRING = "interruption_filter_as_string";
    }

    interface LogColumns {
        String _TIMESTAMP = "timestamp";
        String _TIMESTAMP_AS_STRING = "timestamp_as_string";
        String _USER_UUID = "user_uuid";
    }

    interface NewsColumns {
        String _TITLE = "title";
        String _SUMMARY = "summary";
        String _CONTENT = "content";
    }

    interface StateColumns {
        String _STATE = "state";
    }

    interface TypeColumns {
        String _TYPE = "type";
    }

    interface SortColumns {
        String _ROWID = "rowid";//primary index generated by sqlite automatically
    }

    // Activities table
    public class ActivitiesTable implements LogColumns, ActivityColumns, SortColumns {
        public static final String TABLE_NAME = "activities";

        // Activities creation
        public static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + _TIMESTAMP + " INTEGER,"
                        + _TIMESTAMP_AS_STRING + " TEXT,"
                        + _USER_UUID + " TEXT,"
                        + _IN_VEHICLE_CONFIDENCE + " INTEGER,"
                        + _ON_BICYCLE_CONFIDENCE + " INTEGER,"
                        + _ON_FOOT_CONFIDENCE + " INTEGER,"
                        + _RUNNING_CONFIDENCE + " INTEGER,"
                        + _STILL_CONFIDENCE + " INTEGER,"
                        + _TILTING_CONFIDENCE + " INTEGER,"
                        + _UNKNOWN_CONFIDENCE + " INTEGER,"
                        + _WALKING_CONFIDENCE + " INTEGER);";

    }

    // Calendar events table
    public class CalendarEventsTable implements BaseColumns, LogColumns, CalendarColumns, SortColumns {
        public static final String TABLE_NAME = "calendar_events";

        // Calendar events creation
        public static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + _TIMESTAMP + " INTEGER,"
                        + _TIMESTAMP_AS_STRING + " TEXT,"
                        + _USER_UUID + " TEXT,"
                        + _ID + " INTEGER,"
                        + _START_DATE + " INTEGER,"
                        + _END_DATE + " INTEGER,"
                        + _IS_ALL_DAY + " INTEGER);";
    }

    // Display table
    public class DisplayTable implements LogColumns, StateColumns, SortColumns {
        public static final String TABLE_NAME = "display";

        // Display creation
        public static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + _TIMESTAMP + " INTEGER,"
                        + _TIMESTAMP_AS_STRING + " TEXT,"
                        + _USER_UUID + " TEXT,"
                        + _STATE + " INTEGER);";
    }

    // Interruption filters table
    public class InterruptionFiltersTable implements LogColumns, InterruptionFilterColumns, SortColumns {
        public static final String TABLE_NAME = "interruption_filters";

        // Interruption filters creation
        public static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + _TIMESTAMP + " INTEGER,"
                        + _TIMESTAMP_AS_STRING + " TEXT,"
                        + _USER_UUID + " TEXT,"
                        + _INTERRUPTION_FILTER + " INTEGER,"
                        + _INTERRUPTION_FILTER_AS_STRING + " TEXT);";
    }

    // Network table
    public class NetworkTable implements LogColumns, StateColumns, SortColumns, TypeColumns {
        public static final String TABLE_NAME = "network";

        // Network creation
        public static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + _TIMESTAMP + " INTEGER,"
                        + _TIMESTAMP_AS_STRING + " TEXT,"
                        + _USER_UUID + " TEXT,"
                        + _STATE + " INTEGER,"
                        + _TYPE + " INTEGER);";
    }

    // News table
    public class NewsTable implements BaseColumns, LogColumns, NewsColumns, TypeColumns, SortColumns {
        public static final String TABLE_NAME = "news";

        // News creation
        public static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + _TIMESTAMP + " INTEGER,"
                        + _TIMESTAMP_AS_STRING + " TEXT,"
                        + _ID + " TEXT,"
                        + _TITLE + " TEXT,"
                        + _SUMMARY + " TEXT,"
                        + _CONTENT + " TEXT,"
                        + _TYPE + " INTEGER);";
    }

    // Pending Log Events table
    public class PendingLogEventsTable {
        public static final String TABLE_NAME = "pending_logs";
        public static final String _EVENT_ROW_ID = "event_row_id";

        // Pending Log Events creation
        public static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
                        + _EVENT_ROW_ID + " INTEGER );";
    }

    // Pending News table
    public class PendingNewsTable implements BaseColumns, TypeColumns, SortColumns {
        public static final String TABLE_NAME = "pending_news";

        public static final String _NEWS_ROW_ID = "news_row_id";
        public static final String _USER_UUID = "user_uuid";

        // Pending News creation
        public static final String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + _USER_UUID + " TEXT,"
                        + _NEWS_ROW_ID + " INTEGER, "
                        + _TYPE + " INTEGER);";
    }//class PendingNewsTable

    // UI Event table
    public interface LogEntryTable extends LogColumns, SortColumns {
        String TABLE_NAME = "ui_event";
        String _USED_ALGORITHM = "used_algorithm";
        String _NEWS_ROW_ID = "news_row_id";
        String _UI_ACTION = "ui_action";
        String _SENSOR_ACTIVITY = "sensor_activity";
        String _SENSOR_ACTIVITIES = "sensor_activities";
        String _INTERRUPTION_FILTER = "interruption_filter";
        String _NETWORK_STATE = "network_state";
        String _NETWORK_RSSI = "network_rssi";
        String _DISPLAY = "display";
        String _CAL_PREV_START = "cal_prev_start";
        String _CAL_PREV_END = "cal_prev_end";
        String _CAL_NEXT_START = "cal_next_start";
        String _CAL_NEXT_END = "cal_next_end";
        String _CAL_TODAY_ENTRIES = "cal_today_entries";
        String _CAL_CURRENT_ENTRIES = "cal_current_entries";

        // UI Event creation
        String TABLE_CREATE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        + _TIMESTAMP + " INTEGER,"
                        + _TIMESTAMP_AS_STRING + " TEXT,"
                        + _USER_UUID + " TEXT,"
                        + _USED_ALGORITHM + " INTEGER,"
                        + _NEWS_ROW_ID + " INTEGER,"
                        + _UI_ACTION + " INTEGER,"
                        + _SENSOR_ACTIVITY + " INTEGER,"
                        + _SENSOR_ACTIVITIES + " TEXT,"
                        + _INTERRUPTION_FILTER + " INTEGER,"
                        + _NETWORK_STATE + " INTEGER,"
                        + _NETWORK_RSSI + " INTEGER,"
                        + _DISPLAY + " INTEGER,"
                        + _CAL_PREV_START + " INTEGER,"
                        + _CAL_PREV_END + " INTEGER,"
                        + _CAL_NEXT_START + " INTEGER,"
                        + _CAL_NEXT_END + " INTEGER,"
                        + _CAL_TODAY_ENTRIES + " INTEGER,"
                        + _CAL_CURRENT_ENTRIES + " INTEGER"
                        +");";
    }//interface LogEntryTable
}//class DatabaseContract