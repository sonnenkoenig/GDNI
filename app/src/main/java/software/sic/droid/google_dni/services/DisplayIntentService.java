package software.sic.droid.google_dni.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.WakefulBroadcastReceiver;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.sensors.ScreenSensor;

/*
 * sic.software
 * google-dni_smart_news
 *
 * @date Mär 2017
 */
public class DisplayIntentService extends IntentService {
    // Log tag
    private static final String TAG = DisplayIntentService.class.getSimpleName();
    // Interval for update requests (5 minutes)
    private static final long INTERVAL = 5 * 60 * 1000;

    public DisplayIntentService() {
        super(TAG);
    }//c'tor

    /**
     * Sets the timer for the interval in which this service should be called and registers the service to the alarm
     * manager
     *
     * @param context The context in which the service is created
     */
    public static void queueTimer(Context context) {
        // Get the systems alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // Create the pending intent to receive the broadcast
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(DisplayIntentService.class
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
    public static void startService(Context context) {
        Intent intent = new Intent(context, DisplayIntentService.class);
        WakefulBroadcastReceiver.startWakefulService(context, intent);
    }//startService

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // Get the application
        final MyApplication myApp = MyApplication.instance();
        // Get the database
        Database database = myApp.acquireDatabase();
        // Save all entries in the database
        database.createDisplayState(ScreenSensor.isDisplayActive(this));
        // Close the database after all action are completed to prevent leaks
        myApp.releaseDatabase();
    }//onHandleIntent
}//DisplayIntentService
