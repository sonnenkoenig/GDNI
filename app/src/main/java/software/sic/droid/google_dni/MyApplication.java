/*
 * www.sic.software
 *
 * @file MyApplication.java
 * @date 2017-01-25
 * @brief own app class so we can initialize our singletons here
 */
package software.sic.droid.google_dni;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.Random;

import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.RssFeedReader;
import software.sic.droid.google_dni.data.data.DebugAppStatus;
import software.sic.droid.google_dni.data.data.DebugStatistic;
import software.sic.droid.google_dni.data.data.SensorStatus;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.UiEvent;
import software.sic.droid.google_dni.notification.NotificationHandler;
import software.sic.droid.google_dni.services.ActivityIntentServiceRegistration;
import software.sic.droid.google_dni.services.CalendarEventIntentService;
import software.sic.droid.google_dni.services.DatabaseService;
import software.sic.droid.google_dni.services.DisplayIntentService;
import software.sic.droid.google_dni.services.InterruptionFilterIntentService;
import software.sic.droid.google_dni.services.SmartNewsService;
import software.sic.droid.google_dni.simulation.NewsSimulator;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

public final class MyApplication extends Application {
    final static String TAG = "MyApplication";
    public static final Random sRandom = new Random();
    @SuppressWarnings("CanBeFinal")
    private static MyApplication sInstance;
    {
        sInstance = this;
    }
    public final DebugStatistic mStatistic = new DebugStatistic();
    public final DebugAppStatus mStatus = new DebugAppStatus();
    public final Engine mEngine = new Engine();
    public final SmartNewsAlgorithm mSmartNewsAlgorithm = new SmartNewsAlgorithm();
    private final NotificationHandler mNotificationHandler = NotificationHandler.instance();
    private final Handler mHandler = new Handler();
    private Database mDatabase;
    private Runnable mUiUpdater;
    private long mLastPosted;
    private long mNextQueued;
    private Tracker mTracker;


    @NonNull
    public static MyApplication instance() {
        if (null == sInstance) {
            throw new NullPointerException();
        }
        return sInstance;
    }//instance

    /**
     * Initializes the services which run in the background to collect the user information
     */
    private static void initServices(@NonNull Context context) {
        context.startService(new Intent(context, DatabaseService.class));
        // Set timers for the services requested after a delay
        setupQueueTimersForServices(context);
        // Register for activity updates
        requestActivityUpdates(context);
        // Register for display events
        registerDisplayStateChangedReceiver();
    }//initServices

    private static void registerDisplayStateChangedReceiver() {
        instance().registerReceiver(new DisplayStateChangedReceiver(), new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    /**
     * Registers the app for activity updates
     *
     * @param context The context for the activity registration
     */
    private static void requestActivityUpdates(Context context) {
        // Create a new activity update registration
        new ActivityIntentServiceRegistration(context);
    }//requestActivityUpdates

    /**
     * Setup the queue times for the specific services. The services will be called in the given interval
     *
     * @param context The context in which the services are registered
     */
    private static void setupQueueTimersForServices(Context context) {
        if(DEBUG){Log.d(TAG,"setupQueueTimersForServices()");}
        NewsSimulator.queueTimer(context);
        DisplayIntentService.queueTimer(context);
        RssFeedReader.queueTimer(context);
        SmartNewsService.queueTimer(context);
        CalendarEventIntentService.queueTimer(context);
        InterruptionFilterIntentService.queueTimer(context);
    }//setupQueueTimersForServices

    /**
     * Gets a tracker which is used to track information for google analytics
     *
     * @return The tracker
     */
    public synchronized Tracker getDefaultTracker() {
        if (this.mTracker == null) {
            // Get the instance to google analytics
            GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(this);
            // Create a new tracker
            this.mTracker = googleAnalytics.newTracker(R.xml.global_tracker);
        }
        // Return the tracker
        return this.mTracker;
    }//getDefaultTracker

    public void showNotification(@NonNull NewsAndSensor aNewsAndSensor) {
        mNotificationHandler.showNotification(this, aNewsAndSensor.newsEvent);
        UiEvent uiEvent = new UiEvent(aNewsAndSensor.newsEvent, SmartNewsSharedPreferences.instance().getUserId(),
                UiEvent.Action.NOTIFIED);
        mEngine.onUiEvent(uiEvent, aNewsAndSensor.sensorStatus);
    }//showNotification

    @Override
    public void onCreate() {
        if(DEBUG){Log.d(TAG,"onCreate()");}
        super.onCreate();
        //TODO das ist noch nicht ganz sauber
        //im c'tor von SmartNewsSharedPreferences wird DebugAppStatus.mNewsSimulationIsActivated gesetzt
        //das gehört eigentlich in den c'tor von DebugAppStatus
        SmartNewsSharedPreferences.instance();
        // Initialize background services
        initServices(instance());
        mEngine.notifyCloudBackup();
    }//onCreate

    public void registerUiUpdater(@NonNull Runnable updater) {
        mUiUpdater = updater;
        postUpdateUi();
    }//registerUiUpdater

    public void unRegisterUiUpdater(@NonNull Runnable updater) {
        if (mUiUpdater == updater) {
            mUiUpdater = null;
            mHandler.removeCallbacks(updater);
        }
    }//unRegisterUiUpdater

    public void postUpdateUi() {
        if ((SystemClock.elapsedRealtime() - mLastPosted) > 1000) {//letztes Update war vor über 1s -> UpdateNow
            mLastPosted = SystemClock.elapsedRealtime();
            postUpdateUiDelayed(0);
        } else if ((mNextQueued - SystemClock.elapsedRealtime()) > 1000) {//nächstes geplantes is >1s -> queue in 1s
            postUpdateUiDelayed(1000);
        }
    }//postUpdateUi

    private void postUpdateUiDelayed(long delay) {
        final Runnable updater;
        if (null != (updater = mUiUpdater)) {
            mHandler.removeCallbacks(updater);
            mNextQueued = SystemClock.elapsedRealtime() + delay;
            mHandler.postDelayed(updater, delay);
        }
    }//postUpdateUiDelayed

    public Database acquireDatabase() {
        synchronized (mStatistic) {
            if (null == mDatabase) {
                mDatabase = Database.doCreate(this);
            }
            mStatistic.mDatabaseAcquireCount++;
            mStatistic.mDatabaseConnectionsCount++;
        }
        return mDatabase;
    }//acquireDatabase

    public void releaseDatabase() {
        synchronized (mStatistic) {
            mStatistic.mDatabaseConnectionsCount--;
        }
    }//releaseDatabase

    public static class NewsAndSensor {
        public final
        @NonNull
        NewsEvent newsEvent;
        public final
        @NonNull
        SensorStatus sensorStatus;

        public NewsAndSensor(@NonNull NewsEvent aNewsEvent, @NonNull SensorStatus aSensorStatus) {
            sensorStatus = aSensorStatus;
            newsEvent = aNewsEvent;
        }//c'tor
    }//class NewsAndSensor

    public static final class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MyApplication.instance().mStatistic.mAlarmReceiverCount++;
            final String action = intent.getAction();
            if(DEBUG){ Log.d(TAG,"onAlarm "+action+" mAlarmReceiverCount="+MyApplication.instance().mStatistic.mAlarmReceiverCount);}
            if (NewsSimulator.class.getCanonicalName().equals(action)) {
                NewsSimulator.startService(context);
            } else if (RssFeedReader.class.getCanonicalName().equals(action)) {
                RssFeedReader.startService(context);
            } else if (SmartNewsService.class.getCanonicalName().equals(action)) {
                SmartNewsService.startService(context);
            } else if (CalendarEventIntentService.class.getCanonicalName().equals(action)) {
                CalendarEventIntentService.startService(context);
            } else if (InterruptionFilterIntentService.class.getCanonicalName().equals(action)) {
                InterruptionFilterIntentService.startService(context);
            } else if (DisplayIntentService.class.getCanonicalName().equals(action)) {
                DisplayIntentService.startService(context);
            }
        }//onReceive
    }//AlarmReceiver

    public static final class BootCompletedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            initServices(context);
            MyApplication.instance().mStatus.mBootDetected=true;
        }//onReceive
    }//BootCompletedReceiver

    public static final class DisplayStateChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }//onReceive
    }//DisplayStateChangedReceiver
}//class MyApplication
//#############################################################################
//eof MyApplication.java
