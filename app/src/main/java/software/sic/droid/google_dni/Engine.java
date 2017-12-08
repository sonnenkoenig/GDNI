/*
 * www.sic.software
 *
 * @file Engine.java
 * @date 2017-01-25
 * @brief thread save event handler
 */
package software.sic.droid.google_dni;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.location.DetectedActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.data.SensorStatus;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.UiEvent;
import software.sic.droid.google_dni.sensors.NetworkSensor;
import software.sic.droid.google_dni.sensors.ScreenSensor;
import software.sic.droid.google_dni.services.ActivityIntentService;
import software.sic.droid.google_dni.services.CalendarEventIntentService;
import software.sic.droid.google_dni.services.InterruptionFilterIntentService;
import software.sic.droid.google_dni.tracking.CloudBackup;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static android.content.ContentValues.TAG;
import static software.sic.droid.google_dni.BuildConfig.DEBUG;

public final class Engine {
    private final MyApplication mMyApplication = MyApplication.instance();
    private final SensorStatus mSensorStatusForDebugScreen = new SensorStatus();
    private String mLastSensorStatusAsText;

    public void notifyCloudBackup() {
        if (!mMyApplication.mStatus.mCloudBackupIsActive) {
            mMyApplication.mStatus.mCloudBackupIsActive = true;
            CloudBackup.startService(mMyApplication.getApplicationContext());
        }
    }//notifyCloudBackup

    //news received from Backend
    public void onNewsEvent(NewsEvent newsEvent) {
        Calendar calendarPrevDay = new GregorianCalendar();
        calendarPrevDay.setTime(new Date());
        calendarPrevDay.add(Calendar.HOUR_OF_DAY, -12);

        if (newsEvent.timeStamp > calendarPrevDay.getTimeInMillis()) {
            Database database = mMyApplication.acquireDatabase();
            if (database.addNews(newsEvent)) {
                database.addPendingNewsEntry(SmartNewsSharedPreferences.instance().getUserId(), newsEvent);

                //damit wir im Log auch sehen wann die news empfangen wurde
                onUiEvent( new UiEvent(newsEvent, SmartNewsSharedPreferences.instance().getUserId(), UiEvent.Action.RECEIVED));

                notifyCloudBackup();
                mMyApplication.mStatistic.mOnNewsEventCount++;
            }
            mMyApplication.releaseDatabase();
        }

    }//onNewsEvent

    //user has interacted with a news
    public void onUiEvent(@NonNull UiEvent aUiEvent) {
        SensorStatus sensorStatus = getSensorStatus(aUiEvent.timeStamp, aUiEvent.userId);
        onUiEvent(aUiEvent, sensorStatus);
    }//onUiEvent

    //user has interacted with a news
    public void onUiEvent(@NonNull UiEvent aUiEvent, @NonNull SensorStatus aSensorStatus) {
        Database database = mMyApplication.acquireDatabase();
        if (aUiEvent.action == UiEvent.Action.NOTIFIED) {
            final long replacedNewsRowId = database.getReplacedNewsRowId(aUiEvent.userId);
            if (replacedNewsRowId >= 0) {
                database.addLogEntry(aSensorStatus, aUiEvent.timeStamp, aUiEvent.userId, replacedNewsRowId, UiEvent.Action.REPLACED,
                        aUiEvent.algorithm);

                if (SmartNewsSharedPreferences.instance().getUserId().equals(aUiEvent.userId)) {
                    this.mMyApplication.getDefaultTracker().send(new HitBuilders.EventBuilder().setAction(UiEvent.Action.REPLACED.name())
                            .setCategory(aUiEvent.algorithm.name()).setLabel(aUiEvent.newsEvent.id).build());
                }
            }
        }
        if (SmartNewsSharedPreferences.instance().getUserId().equals(aUiEvent.userId)) {
            this.mMyApplication.getDefaultTracker().send(new HitBuilders.EventBuilder().setAction(aUiEvent.action.name())
                    .setCategory(aUiEvent.algorithm.name()).setLabel(aUiEvent.newsEvent.id).build());
        }

        database.addLogEntry(aUiEvent, aSensorStatus);
        mMyApplication.releaseDatabase();
        notifyCloudBackup();
    }//onUiEvent

    @SuppressWarnings("unused")
    @NonNull
    public SensorStatus getSensorStatus() {
        final String userId = SmartNewsSharedPreferences.instance().getUserId();
        return getSensorStatus(System.currentTimeMillis(), userId);
    }//getSensorStatus

    @Nullable
    public String getLastSensorStatusAsText(){
        if( null == mLastSensorStatusAsText ){
            mLastSensorStatusAsText = mSensorStatusForDebugScreen.toString();
        }
        return mLastSensorStatusAsText;
    }//getLastSensorStatusAsText

    public synchronized void onActivityState(@NonNull List<DetectedActivity> aDetectedActivities) {
        mMyApplication.mStatistic.mActivityEventCount++;
        if( null == mSensorStatusForDebugScreen.activitiesStatus){//lazy creation
            mSensorStatusForDebugScreen.activitiesStatus = new SensorStatus.ActivitiesStatus();
        }
        //update timestamp
        mSensorStatusForDebugScreen.activitiesStatus.timeStamp = System.currentTimeMillis();

        ActivityIntentService.DetectedActivities newActivities = new ActivityIntentService.DetectedActivities();
        mSensorStatusForDebugScreen.activitiesStatus.detectedActivitiesState = newActivities.set(aDetectedActivities);
        final boolean hasChanged = !newActivities.equals(mSensorStatusForDebugScreen.activitiesStatus.detectedActivities);

        //write to DB only if status has changed
        if( hasChanged){
            mSensorStatusForDebugScreen.activitiesStatus.detectedActivities = newActivities;
            Database database = mMyApplication.acquireDatabase();
            // Save the detected activities to the database
            database.addActivityState(aDetectedActivities);
            // Close the database
            mMyApplication.releaseDatabase();
        }
        if(DEBUG){Log.v(TAG,"onActivityState " + newActivities + " changed="+hasChanged);}

        //trigger update ui
        mLastSensorStatusAsText = null;//see getLastSensorStatusAsText
        mMyApplication.postUpdateUi();
    }//onActivityState

    @NonNull
    SensorStatus getSensorStatus(final long aTime, @NonNull final String aUserId) {
        Database database = mMyApplication.acquireDatabase();
        try {
            SensorStatus sensorStatus = new SensorStatus();

            sensorStatus.activitiesStatus = database.getActivitiesStatus(aUserId);
            CalendarEventIntentService.updateCalendarDatabase(mMyApplication);
            sensorStatus.calendarStatus = database.getCalendarStatus(aTime, aUserId);
            sensorStatus.displayStatus = ScreenSensor.getDisplayStatus(this.mMyApplication);
            sensorStatus.interruptionFilterStatus = InterruptionFilterIntentService.getInterruptionFilterStatus
                    (this.mMyApplication);
            sensorStatus.networkStatus = NetworkSensor.getNetworkStatus(this.mMyApplication);

            if( aUserId.equals( SmartNewsSharedPreferences.instance().getUserId())){
                mSensorStatusForDebugScreen.displayStatus = sensorStatus.displayStatus;
                mSensorStatusForDebugScreen.interruptionFilterStatus = sensorStatus.interruptionFilterStatus;
                mSensorStatusForDebugScreen.calendarStatus = sensorStatus.calendarStatus;
                mSensorStatusForDebugScreen.networkStatus = sensorStatus.networkStatus;
                if( null == mSensorStatusForDebugScreen.activitiesStatus) {
                    mSensorStatusForDebugScreen.activitiesStatus = sensorStatus.activitiesStatus;
                }
                mLastSensorStatusAsText = null;
            }
            return sensorStatus;
        } finally {
            mMyApplication.releaseDatabase();
        }
    }//getSensorStatus
}//class Engine
//#############################################################################
//eof Engine.java

