/*
 * sic.software
 * google-dni_smart_news
 *
 * @date Feb 2017
 */
package software.sic.droid.google_dni.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.WakefulBroadcastReceiver;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.data.SensorStatus;

/**
 * Class to find out what interruption filter is currently set on the device. The interruption filter is set by the
 * user as he is activating the "do not disturb" mode on his device.
 */
public class InterruptionFilterIntentService extends IntentService {
    // Constants for the interruption filter
    public static final int INTERRUPTION_FILTER_UNKNOWN = 0;
    public static final int INTERRUPTION_FILTER_ALL = 1;
    public static final int INTERRUPTION_FILTER_PRIORITY = 2;
    public static final int INTERRUPTION_FILTER_NONE = 3;
    public static final int INTERRUPTION_FILTER_ALARMS = 4;
    // Log tag
    private static final String TAG = InterruptionFilterIntentService.class.getSimpleName();
    // Interval 5 minutes
    private static final long INTERVAL = 5 * 60 * 1000;
    // Constant for the zen mode identifier
    private static final String ZEN_MODE = "zen_mode";

    public InterruptionFilterIntentService() {
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
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent
                (InterruptionFilterIntentService.class.getCanonicalName(), null, context, MyApplication.AlarmReceiver
                        .class), 0);
        // Hand over the pending intent to the alarm manger and set the interval
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + INTERVAL,
                INTERVAL, pendingIntent);
    }//queueTimer

    /**
     * Starts the service
     *
     * @param context The context in which the service is started
     */
    public static void startService(Context context) {
        // Create the intent for the intent service
        Intent intent = new Intent(context, InterruptionFilterIntentService.class);
        // Pass the intent service to the wakeful service
        WakefulBroadcastReceiver.startWakefulService(context, intent);
    }//startService

    /**
     * Gets the current interruption filter of the device
     *
     * @param context The context
     * @return Integer value of the interruption filter
     */
    public static int getCurrentInterruptionFilter(@NonNull Context context) {
        // Get the system notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);
        // Field to store the current interruption filter in
        int interruptionFilter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6 and above
            // Get the currently active interruption filter from the notification manager
            interruptionFilter = notificationManager.getCurrentInterruptionFilter();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Android 5
            try {
                // Get the currently active zen mode of the device
                int zenMode = Settings.Global.getInt(context.getApplicationContext().getContentResolver(), ZEN_MODE);
                // Match the currently active zen mode to the right interruption filter
                interruptionFilter = zenModeToInterruptionFilter(zenMode);
            } catch (Settings.SettingNotFoundException e) {
                // In case no settings were found we handle the zen mode as unknown
                interruptionFilter = INTERRUPTION_FILTER_UNKNOWN;
            }
        } else {
            // Everything prior android 5 does not have the do not disturb feature so we return the unknown value
            interruptionFilter = INTERRUPTION_FILTER_UNKNOWN;
        }
        // Return the interruption filter
        return interruptionFilter;
    }//getCurrentInterruptionFilter

    /**
     * Matches the zen mode, which comes from android version 5, value to the corresponding interruption filter for
     * android version 6 and above
     *
     * @param zenMode The value of the zen mode got from the device
     * @return The value for the interruption filter
     */
    private static int zenModeToInterruptionFilter(int zenMode) {
        switch (zenMode) {
            case 0:
                return INTERRUPTION_FILTER_ALL;
            case 1:
                return INTERRUPTION_FILTER_PRIORITY;
            case 2:
                return INTERRUPTION_FILTER_NONE;
            case 3:
                return INTERRUPTION_FILTER_ALARMS;
            default:
                return INTERRUPTION_FILTER_UNKNOWN;
        }
    }//zenModeToInterruptionFilter

    /**
     * Matches the int of the interruption filter to the corresponding enum
     *
     * @param interruptionFilter The interruption filter in form of an integer
     * @return The enum value of the interruption filter
     */
    private static InterruptionFilters interruptionFilterIntToEnum(int interruptionFilter) {
        switch (interruptionFilter) {
            case INTERRUPTION_FILTER_ALL:
                return InterruptionFilters.ALL;
            case INTERRUPTION_FILTER_PRIORITY:
                return InterruptionFilters.PRIORITY;
            case INTERRUPTION_FILTER_NONE:
                return InterruptionFilters.NONE;
            case INTERRUPTION_FILTER_ALARMS:
                return InterruptionFilters.ALARMS;
            default:
                return InterruptionFilters.UNKNOWN;
        }
    }//interruptionFilterIntToEnum

    /**
     * Gets the interruption filter status out of the database. The interruption filter status holds all the important
     * information used in the algorithm
     *
     * @return The interruption filter status for the given user
     */
    @NonNull
    public static SensorStatus.InterruptionFilterStatus getInterruptionFilterStatus(@NonNull final Context context) {
        // Create the interruption filter status
        SensorStatus.InterruptionFilterStatus interruptionFilterStatus = new SensorStatus.InterruptionFilterStatus();
        // If the interruption filter is not saved in the database already we return unknown
        interruptionFilterStatus.interruptionFilter = interruptionFilterIntToEnum(getCurrentInterruptionFilter
                (context));
        // Return the network status
        return interruptionFilterStatus;
    }//getInterruptionFilterStatus

    @Override
    protected void onHandleIntent(Intent intent) {
        // Store the interruption filter in database
        this.createInterruptionFilterInDatabase(getCurrentInterruptionFilter(this));
        // Complete the wakeful intent
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }//onHandleIntent

    /**
     * Stores the given interruption filter in the database
     *
     * @param interruptionFilter The interruption filter to be stored in the database
     */
    private void createInterruptionFilterInDatabase(int interruptionFilter) {
        // Get the database
        final MyApplication myApp = MyApplication.instance();
        Database database = myApp.acquireDatabase();
        // Store the interruption filter in the database
        database.createInterruptionFilter(interruptionFilter);
        // Close the database after all action are completed to prevent leaks
        myApp.releaseDatabase();
    }//createInterruptionFilterInDatabase

    public enum InterruptionFilters {
        UNKNOWN, // Unknown
        ALL, // All notifications will be shown
        PRIORITY, // Notification from apps with priority are not suppressed
        NONE, // All notification are suppressed
        ALARMS; // Alarm notification are not suppressed

        public static
        @Nullable
        InterruptionFilters valueOf(int a) {
            InterruptionFilters[] t = InterruptionFilters.values();
            if (a < 0 || a >= t.length) {
                return null;
            }
            return t[a];
        }//valueOf
    }//InterruptionFilters
}//InterruptionFilterIntentService
