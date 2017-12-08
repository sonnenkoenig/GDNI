package software.sic.droid.google_dni.data.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;

import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.UiEvent;
import software.sic.droid.google_dni.sensors.NetworkSensor;
import software.sic.droid.google_dni.services.ActivityIntentService;
import software.sic.droid.google_dni.services.InterruptionFilterIntentService;

/**
 * www.sic.software
 *
 * 17.03.17
 */

public class LogEntry {
    public final long timestamp;
    public final String userId;
    public final UiEvent.Action uiAction;
    public final Algorithm algorithm;
//    public long newsRowId;
    public String newsId;
    public NewsEvent.Type newsType;
    public final SensorStatus sensorStatus;
    public final String detectedActivitiesAsString;//quick and dirty hack - ich will das ganze array im log

    public enum Algorithm {SMART, INVERTED, NONE;
        public static int sDebugOffset;
        public static @Nullable
        Algorithm valueOf(int a){
            Algorithm[] t = Algorithm.values();
            if(a<0||a>=t.length){return null;}
            return t[a];
        }//valueOf

        @Nullable
        public static Algorithm fromUserId( @Nullable String aUserId){
            if(null == aUserId){return null;}
            return fromUserId(aUserId, System.currentTimeMillis());
        }//fromUserId

        @NonNull
        public static Algorithm fromUserId( @NonNull String aUserId, long aTime){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(aTime);
            int random=calendar.get(Calendar.DAY_OF_YEAR)+sDebugOffset;
            for( int b : aUserId.getBytes()){
                random+=b;
            }
            //noinspection ConstantConditions
            return valueOf( Math.abs(random) % Algorithm.values().length);
        }//fromUserId
    }//enum Algorithm

    public LogEntry(@NonNull Cursor cursor , @NonNull SQLiteDatabase database) {
        timestamp = cursor.getLong(cursor.getColumnIndex(DatabaseContract.LogEntryTable._TIMESTAMP));
        uiAction = UiEvent.Action.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._UI_ACTION)));
        userId = cursor.getString(cursor.getColumnIndex(DatabaseContract.LogEntryTable._USER_UUID));
        algorithm = Algorithm.valueOf( cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._USED_ALGORITHM)));

        sensorStatus = new SensorStatus();
        sensorStatus.activitiesStatus = new SensorStatus.ActivitiesStatus();
        sensorStatus.activitiesStatus.detectedActivitiesState = ActivityIntentService.DetectedActivityState.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._SENSOR_ACTIVITY)));
        detectedActivitiesAsString = cursor.getString(cursor.getColumnIndex(DatabaseContract.LogEntryTable._SENSOR_ACTIVITIES));
        sensorStatus.interruptionFilterStatus = new SensorStatus.InterruptionFilterStatus();
        sensorStatus.interruptionFilterStatus.interruptionFilter = InterruptionFilterIntentService.InterruptionFilters.valueOf( cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._INTERRUPTION_FILTER)));

        sensorStatus.networkStatus = new SensorStatus.NetworkStatus();
        sensorStatus.networkStatus.networkState = NetworkSensor.NetworkState.valueOf( cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._NETWORK_STATE )));
        sensorStatus.networkStatus.signalStrength = cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._NETWORK_RSSI));

        sensorStatus.displayStatus = new SensorStatus.DisplayStatus();
        sensorStatus.displayStatus.isDisplayActive = cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._DISPLAY ))!=0;
        sensorStatus.calendarStatus = new SensorStatus.CalendarStatus();
        sensorStatus.calendarStatus.previousStartTime=cursor.getLong(cursor.getColumnIndex(DatabaseContract.LogEntryTable._CAL_PREV_START));
        sensorStatus.calendarStatus.previousEndTime=cursor.getLong(cursor.getColumnIndex(DatabaseContract.LogEntryTable._CAL_PREV_END) );
        sensorStatus.calendarStatus.nextStartTime=cursor.getLong(cursor.getColumnIndex(DatabaseContract.LogEntryTable._CAL_NEXT_START ));
        sensorStatus.calendarStatus.nextEndTime=cursor.getLong(cursor.getColumnIndex(DatabaseContract.LogEntryTable._CAL_NEXT_END ));
        sensorStatus.calendarStatus.todayEntries=cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._CAL_TODAY_ENTRIES ));
        sensorStatus.calendarStatus.currentEntries=cursor.getInt(cursor.getColumnIndex(DatabaseContract.LogEntryTable._CAL_CURRENT_ENTRIES ));


        long newsRowId  = cursor.getLong(cursor.getColumnIndex(DatabaseContract.LogEntryTable._NEWS_ROW_ID));
        cursor.close();

        //read NewsEntry
        cursor = database.rawQuery(
                "SELECT "+DatabaseContract.NewsTable._ID+","+DatabaseContract.NewsTable._TYPE+" FROM "+DatabaseContract.NewsTable.TABLE_NAME+" WHERE rowid="+newsRowId
                ,null//String[] selectionArgs
        );
        if(cursor.moveToFirst()){
            newsId = cursor.getString(cursor.getColumnIndex(DatabaseContract.NewsTable._ID));
            newsType = NewsEvent.Type.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseContract.NewsTable._TYPE)));
        }
        cursor.close();

    }//c'tor

}//class LogEntry
