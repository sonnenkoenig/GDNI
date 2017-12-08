/* www.sic.software
 *
 * @file UserSimulator.java
 * @date 2017-03-27
 * @brief ???
 */
package software.sic.droid.google_dni.simulation;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.TimeZone;
import java.util.UUID;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.data.CalendarEvent;
import software.sic.droid.google_dni.data.data.SensorStatus;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.UiEvent;
import software.sic.droid.google_dni.sensors.NetworkSensor;
import software.sic.droid.google_dni.services.ActivityIntentService;
import software.sic.droid.google_dni.services.InterruptionFilterIntentService;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

//import android.support.annotation.Nullable;


public class UserSimulator {
    private static final String TAG = "UserSimulator";
    private final static int K_MAX_SIMULATED_USERS = 100;
    private static final long K_MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    private static long sLastUsedID;
    private final String mSimulatedUserIds[];
    private final MyApplication mMyApplication = MyApplication.instance();

    {
        mSimulatedUserIds = new String[K_MAX_SIMULATED_USERS];
        final String userID = SmartNewsSharedPreferences.instance().getUserId();
        for (int i = 0; i < mSimulatedUserIds.length; i++) {
            String simulatedName = "software.sic.droid.google_dni.simulation." + userID + "." + i;
            mSimulatedUserIds[i] = UUID.nameUUIDFromBytes(simulatedName.getBytes()).toString();
        }
    }

    public static <T extends Enum<T>> T doGetRandomEnum(T[] values) {
        return values[MyApplication.sRandom.nextInt(values.length)];
    }

    private void doGetSimulatedSensorStatus(@NonNull Database database, @NonNull SensorStatus simulatedSensorStatus,
                                            @NonNull String simulatedUser, long time) {

        //simulate sensor status

        simulatedSensorStatus.activitiesStatus.detectedActivitiesState = doGetRandomEnum(ActivityIntentService
                .DetectedActivityState.values());
        //80% STILL
        if (MyApplication.sRandom.nextInt(100) < 80) {
            simulatedSensorStatus.activitiesStatus.detectedActivitiesState = ActivityIntentService
                    .DetectedActivityState.STILL;//IN_VEHICLE, ON_BICYCLE, ON_FOOT, RUNNING, STILL, TILTING, UNKNOWN,
            // WALKING
        }

        simulatedSensorStatus.calendarStatus = database.getCalendarStatus(time, simulatedUser);
        if (simulatedSensorStatus.calendarStatus.todayEntries == 0) {
            doSimulateCalendarEntries(database, time, simulatedUser);
            simulatedSensorStatus.calendarStatus = database.getCalendarStatus(time, simulatedUser);
        }
//                    simulatedSensorStatus.calendarStatus.currentEntries = 0;//
//                    simulatedSensorStatus.calendarStatus.todayEntries = MyApplication.sRandom.nextInt(5);
//                    simulatedSensorStatus.calendarStatus.previousStartTime = time - 3000;
//                    simulatedSensorStatus.calendarStatus.previousEndTime = time - 2000;
//                    simulatedSensorStatus.calendarStatus.nextStartTime = time + 1000;
//                    simulatedSensorStatus.calendarStatus.nextEndTime = time + 2000;

        simulatedSensorStatus.displayStatus.isDisplayActive = MyApplication.sRandom.nextBoolean();

        //UNKNOWN/ALL - all notifications are allowed
        //NONE - no notification is allowed
        simulatedSensorStatus.interruptionFilterStatus.interruptionFilter = doGetRandomEnum
                (InterruptionFilterIntentService.InterruptionFilters.values());//UNKNOWN, ALL, PRIORITY, NONE, ALARMS
        //80% All events
        if (MyApplication.sRandom.nextInt(100) < 80) {
            simulatedSensorStatus.interruptionFilterStatus.interruptionFilter = InterruptionFilterIntentService
                    .InterruptionFilters.ALL;
        }

        simulatedSensorStatus.networkStatus.networkState = doGetRandomEnum(NetworkSensor.NetworkState.values());
        //NONE, WIFI, TWO_G, THREE_G, FOUR_G;
        simulatedSensorStatus.networkStatus.signalStrength = MyApplication.sRandom.nextInt(4);

    }//doGetSimulatedSensorStatus

    void onSimulatedNews(@NonNull NewsEvent dummyNews) {
        if(!mMyApplication.mStatus.mUserSimulationIsActivated){
            return;
        }
        Database database = mMyApplication.acquireDatabase();
        try {
            for (String simulatedUser : mSimulatedUserIds) {
                database.addPendingNewsEntry(simulatedUser, dummyNews);
            }
            int simulatedNotified = 0;
            int lastSimulatedNotified = 0;
            int loopCount = 0;

            SensorStatus simulatedSensorStatus = new SensorStatus();
            simulatedSensorStatus.activitiesStatus = new SensorStatus.ActivitiesStatus();
            simulatedSensorStatus.calendarStatus = new SensorStatus.CalendarStatus();
            simulatedSensorStatus.displayStatus = new SensorStatus.DisplayStatus();
            simulatedSensorStatus.interruptionFilterStatus = new SensorStatus.InterruptionFilterStatus();
            simulatedSensorStatus.networkStatus = new SensorStatus.NetworkStatus();

            MyApplication.NewsAndSensor notifiedNews[] = new MyApplication.NewsAndSensor[K_MAX_SIMULATED_USERS];
            for (int j = 0; j < 20; j++) {
                for (int i = 0; i < K_MAX_SIMULATED_USERS; i++) {
                    String simulatedUser = mSimulatedUserIds[i];
                    loopCount++;

                    long time = System.currentTimeMillis();
                    doGetSimulatedSensorStatus(database, simulatedSensorStatus, simulatedUser, time);

                    //
                    final MyApplication.NewsAndSensor newsAndSensor = mMyApplication.mSmartNewsAlgorithm
                            .getNewsForNotification(time, simulatedUser, simulatedSensorStatus);
                    if (null != newsAndSensor) {
                        notifiedNews[i] = newsAndSensor;
                        UiEvent uiEvent = new UiEvent(newsAndSensor.newsEvent, simulatedUser, UiEvent.Action.NOTIFIED);
                        simulatedNotified++;
                        mMyApplication.mEngine.onUiEvent(uiEvent, newsAndSensor.sensorStatus);
                        //use array with last notified News
                    }
                }//for simulated user(generate News)
                for (int i = 0; i < K_MAX_SIMULATED_USERS; i++) {
                    String simulatedUser = mSimulatedUserIds[i];
                    final MyApplication.NewsAndSensor newsAndSensor = notifiedNews[i];
                    if (null != newsAndSensor) {
                        UiEvent.Action simulatedAction = doGetSimulatedUserAction(newsAndSensor.newsEvent.type,
                                newsAndSensor.sensorStatus);
                        if (null != simulatedAction) {
                            UiEvent uiEvent = new UiEvent(newsAndSensor.newsEvent, simulatedUser, simulatedAction);
                            mMyApplication.mEngine.onUiEvent(uiEvent, newsAndSensor.sensorStatus);
                            simulatedNotified++;
                        }
                        notifiedNews[i] = null;
                    }
                }//for simulated user(show drop or ignore news

                if (lastSimulatedNotified == simulatedNotified) {
                    break;
                }
                lastSimulatedNotified = simulatedNotified;
                if (DEBUG) {
                    Log.d(TAG, "loopCount=" + loopCount + " j=" + j + " simulatedNotified=" + simulatedNotified);
                }
            }
            if (DEBUG) {
                Log.d(TAG, "loopCount=" + loopCount + " simulatedNotified=" + simulatedNotified);
            }
        } finally {
            mMyApplication.releaseDatabase();
        }
//        System.gc();
    }//onSimulatedNews

    /**
     * @return null if user ignores the message
     */
    @Nullable
    private UiEvent.Action doGetSimulatedUserAction(NewsEvent.Type type, SensorStatus sensorStatus) {
        double ignoredWeight = 50;
        double shownWeight = 25;
        double swipedWeight = 25;
        boolean shouldShow=true;

        //bei Video und kein WLAN - steigt die Warscheinlichkeit das es weggewischt wird
        if (type == NewsEvent.Type.VIDEO) {
            if (sensorStatus.networkStatus.networkState != NetworkSensor.NetworkState.WIFI) {
                swipedWeight += 5;
            }
        }

        if (sensorStatus.calendarStatus.currentEntries > 0) {
            swipedWeight += 5;
            ignoredWeight += 5;
            shouldShow = false;
        }else{
            shownWeight += 5;
        }

        if (  sensorStatus.interruptionFilterStatus.interruptionFilter == InterruptionFilterIntentService.InterruptionFilters.ALL
           || sensorStatus.interruptionFilterStatus.interruptionFilter == InterruptionFilterIntentService.InterruptionFilters.UNKNOWN
            ) {
            shownWeight += 5;
        }else{
            ignoredWeight += 5;
            shouldShow = false;
        }

        if (  sensorStatus.activitiesStatus.detectedActivitiesState == ActivityIntentService.DetectedActivityState.STILL
           || sensorStatus.activitiesStatus.detectedActivitiesState == ActivityIntentService.DetectedActivityState.TILTING
           ) {
            shownWeight += 5;
        }else{
            shouldShow = false;
            ignoredWeight += 5;
        }
        if(!shouldShow){
            shownWeight = 25;
        }

        final double allWeight = ignoredWeight + shownWeight + swipedWeight;
        final double random = MyApplication.sRandom.nextDouble() * allWeight;
        if (random <= ignoredWeight) {
            return null;
        }
        if (random <= (ignoredWeight + shownWeight)) {
            return UiEvent.Action.SHOWN;
        }
        return UiEvent.Action.SWIPED;
    }//doGetSimulatedUserAction

    private void doSimulateCalendarEntries(Database database, long time, String simulatedUser) {
        final int count = MyApplication.sRandom.nextInt(10);
        final TimeZone tz = TimeZone.getDefault();
        final int offset = tz.getOffset(time);
        final long midnight = K_MILLIS_PER_DAY * ((time + offset) / K_MILLIS_PER_DAY);
        if (sLastUsedID == 0) {
            sLastUsedID = time;
        }

        for (int i = 0; i < count; i++) {
            long startTime = midnight + 5 * 60 * 1000 * (MyApplication.sRandom.nextInt(24 * 12));//5' Steps
            long endTime = startTime + 5 * 60 * 1000 * (MyApplication.sRandom.nextInt(2 * 12));//5' Steps 0-2h duration
            final boolean isAllDay_false = false;
            CalendarEvent calendarEvent = new CalendarEvent(simulatedUser, ++sLastUsedID, startTime, endTime,
                    isAllDay_false);
            database.createCalendarEvent(calendarEvent);
        }
    }//doSimulateCalendarEntries
}//class UserSimulator
//#############################################################################
//eof UserSimulator.java
