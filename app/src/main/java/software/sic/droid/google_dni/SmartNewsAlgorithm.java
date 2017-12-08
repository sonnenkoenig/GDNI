/* www.sic.software
 *
 * @file SmartNewsAlgorithm.java
 * @date 2017-02-28
 * @brief logic to find best time for Notification
 */
package software.sic.droid.google_dni;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.data.DbEntryNotFoundException;
import software.sic.droid.google_dni.data.data.LogEntry;
import software.sic.droid.google_dni.data.data.SensorStatus;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.PendingNewsEvent;
import software.sic.droid.google_dni.services.ActivityIntentService;
import software.sic.droid.google_dni.services.InterruptionFilterIntentService;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

/**
 * Class to find the perfect time to show a news
 */
public class SmartNewsAlgorithm {
    private static final int WIFI_SIGNAL_STRENGTH_BOTTOM_BORDER = 1;
    private static final int WIFI_SIGNAL_STRENGTH_MIDDLE_BORDER = 2;
    private static final int FOUR_G_SIGNAL_STRENGTH_BOTTOM_BORDER = 1;
    private static final int FOUR_G_SIGNAL_STRENGTH_MIDDLE_BORDER = 2;
    private static final int THREE_G_SIGNAL_STRENGTH_BOTTOM_BORDER = 2;
    private static final int THREE_G_SIGNAL_STRENGTH_MIDDLE_BORDER = 3;

    private final MyApplication mMyApplication = MyApplication.instance();

    @Nullable
    public MyApplication.NewsAndSensor getNewsForNotification() {
        final String userId = SmartNewsSharedPreferences.instance().getUserId();
        return getNewsForNotification(System.currentTimeMillis(), userId, null);
    }//getNewsForNotification

    /**
     * Gets the preferred news type depending on the different sensors
     *
     * @param sensorStatus The sensor status with all information required to calculate if and what news should be shown
     * @return null if User is busy
     */
    @Nullable
    private NewsEvent.Type getPreferredNewsType(@Nullable SensorStatus sensorStatus, @NonNull
            LogEntry.Algorithm algorithm) {
        // Switch over the algorithm to decide which algorithm to use
        switch (algorithm) {
            case SMART:
                return this.useSmartAlgorithm(sensorStatus);
            case INVERTED:
                return this.useInvertedAlgorithm(sensorStatus);
            default:
                return null;
        }
    }//getPreferredNewsType

    @Nullable
    private NewsEvent.Type useSmartAlgorithm(@Nullable SensorStatus sensorStatus) {
        if (!this.shouldShowNews(sensorStatus)) return null;
        // Initialize the return type
        NewsEvent.Type type = null;
        // If there is no sensor status to work with return text as type
        if (null == sensorStatus) return NewsEvent.Type.TEXT;
        if (this.shouldShowNews(sensorStatus)) {
            // If there is no network state always show text
            if (null == sensorStatus.networkStatus) return NewsEvent.Type.TEXT;
            // Determine which type of news event should be shown
            switch (sensorStatus.networkStatus.networkState) {
                case NONE:
                    // Return null if there is no network state
                    return null;
                case WIFI:
                    // Get the strength of the wifi signal
                    int wifiSignalStrength = sensorStatus.networkStatus.signalStrength;
                    // Determine which type to show depending on the signal strength of the wifi signal
                    if (wifiSignalStrength < WIFI_SIGNAL_STRENGTH_BOTTOM_BORDER) {
                        type = NewsEvent.Type.TEXT;
                    } else if (wifiSignalStrength < WIFI_SIGNAL_STRENGTH_MIDDLE_BORDER) {
                        type = NewsEvent.Type.PICTURE;
                    } else {
                        type = NewsEvent.Type.VIDEO;
                    }
                    break;
                case FOUR_G:
                    // Get the strength of the mobile data signal
                    int fourGSignalStrength = sensorStatus.networkStatus.signalStrength;
                    // Determine which type to show depending on the signal strength of the mobile data
                    if (fourGSignalStrength < FOUR_G_SIGNAL_STRENGTH_BOTTOM_BORDER) {
                        type = NewsEvent.Type.TEXT;
                    } else if (fourGSignalStrength < FOUR_G_SIGNAL_STRENGTH_MIDDLE_BORDER) {
                        type = NewsEvent.Type.PICTURE;
                    } else {
                        type = NewsEvent.Type.VIDEO;
                    }
                    break;
                case THREE_G:
                    // Get the strength of the mobile data signal
                    int threeGSignalStrength = sensorStatus.networkStatus.signalStrength;
                    // Determine which type to show depending on the strength of the mobile data signal
                    if (threeGSignalStrength < THREE_G_SIGNAL_STRENGTH_BOTTOM_BORDER) {
                        type = NewsEvent.Type.TEXT;
                    } else if (threeGSignalStrength < THREE_G_SIGNAL_STRENGTH_MIDDLE_BORDER) {
                        type = NewsEvent.Type.PICTURE;
                    } else {
                        type = NewsEvent.Type.VIDEO;
                    }
                    break;
                default:
                    // Set text as the default type
                    type = NewsEvent.Type.TEXT;
                    break;
            }
        }
        // Return the type
        return type;
    }//useSmartAlgorithm

    /**
     * Determines a bad time to shown a news event which basically is the inverted smart algorithm The news gets
     * shown to the user if the re is an activity going on or if he user is busy through being in a appointment or
     * having the interruption filter set
     *
     * @param sensorStatus The sensor status which holds the information about all the sensors used in the algorithm
     * @return The type of the news event to show or null
     */
    @Nullable
    private NewsEvent.Type useInvertedAlgorithm(@Nullable SensorStatus sensorStatus) {
        // If there is no sensor state always show the video message
        if (sensorStatus == null) return NewsEvent.Type.VIDEO;
        // Initialize the return type
        NewsEvent.Type type = null;
        if (!shouldShowNews(sensorStatus)) {
            if (null == sensorStatus.networkStatus) {
                // Return the text type if there is no network state available
                return NewsEvent.Type.TEXT;
            }
            // Determine which type of news event should be shown
            switch (sensorStatus.networkStatus.networkState) {
                case NONE:
                    // Return video if no internet is available for the worst user experience possible
                    type = NewsEvent.Type.VIDEO;
                    break;
                case WIFI:
                    // Get the strength of the wifi signal
                    int wifiSignalStrength = sensorStatus.networkStatus.signalStrength;
                    // Determine which type to show depending on the signal strength
                    if (wifiSignalStrength < WIFI_SIGNAL_STRENGTH_BOTTOM_BORDER) {
                        type = NewsEvent.Type.VIDEO;
                    } else if (wifiSignalStrength < WIFI_SIGNAL_STRENGTH_MIDDLE_BORDER) {
                        type = NewsEvent.Type.PICTURE;
                    } else {
                        type = NewsEvent.Type.TEXT;
                    }
                    break;
                case FOUR_G:
                    // Get the strength of the mobile data signal
                    int fourGSignalStrength = sensorStatus.networkStatus.signalStrength;
                    // Determine which type to show depending in the signal strength
                    if (fourGSignalStrength < FOUR_G_SIGNAL_STRENGTH_BOTTOM_BORDER) {
                        type = NewsEvent.Type.VIDEO;
                    } else if (fourGSignalStrength < FOUR_G_SIGNAL_STRENGTH_MIDDLE_BORDER) {
                        type = NewsEvent.Type.PICTURE;
                    } else {
                        type = NewsEvent.Type.TEXT;
                    }
                    break;
                case THREE_G:
                    // Get the strength of the mobile data signal
                    int threeGSignalStrength = sensorStatus.networkStatus.signalStrength;
                    // Determine which type to show depending in the signal strength
                    if (threeGSignalStrength < THREE_G_SIGNAL_STRENGTH_BOTTOM_BORDER) {
                        type = NewsEvent.Type.VIDEO;
                    } else if (threeGSignalStrength < THREE_G_SIGNAL_STRENGTH_MIDDLE_BORDER) {
                        type = NewsEvent.Type.PICTURE;
                    } else {
                        type = NewsEvent.Type.TEXT;
                    }
                    break;
                default:
                    // Set video as default type
                    type = NewsEvent.Type.VIDEO;
                    break;
            }
        }
        // Return the type
        return type;
    }//useInvertedAlgorithm

    /**
     * Evaluates if the news should be shown to the user.
     *
     * @param sensorStatus The sensor status with all information required to calculate if and what news should be shown
     * @return Boolean value if the news should be shown to the user or not
     */
    private boolean shouldShowNews(SensorStatus sensorStatus) {
        // Boolean flag to identify if the news should be shown or not
        boolean shouldShow = true;
        // Check if activity is active, interruption filter is set or an calendar event is actually running
        if (this.isActivityActive(sensorStatus.activitiesStatus.detectedActivitiesState) || this
                .isInterruptionFilterSet(sensorStatus.interruptionFilterStatus.interruptionFilter) || sensorStatus
                .calendarStatus.currentEntries > 0) {
            shouldShow = false;
        }
        // Return if the news should be shown or not
        return shouldShow;
    }//shouldShowNews

    /**
     * Checks if the last detected activity is one which makes the user unable to respond to an notification
     *
     * @param detectedActivityState The last detected activity
     * @return If an activity is active or not
     */
    private boolean isActivityActive(ActivityIntentService.DetectedActivityState detectedActivityState) {
        return !(detectedActivityState == ActivityIntentService.DetectedActivityState.TILTING ||
                detectedActivityState == ActivityIntentService.DetectedActivityState.STILL);
    }//isActivityActive

    /**
     * Checks if an interruption filter is set
     *
     * @param interruptionFilter The integer value of the interruption filter
     * @return Boolean value depending of the state of the interruption filter we return false if no interruption
     * filter is set or the state of the interruption filter is unknown
     */
    private boolean isInterruptionFilterSet(InterruptionFilterIntentService.InterruptionFilters interruptionFilter) {
        switch (interruptionFilter) {
            case ALL:
                return false;
            case UNKNOWN:
                return false;
            default:
                return true;
        }
    }//isInterruptionFilterSet

    /**
     * @param aTime for simulation
     * @return News Entry with preferred type set in NewsEvent#type.
     */
    @Nullable
    public MyApplication.NewsAndSensor getNewsForNotification(final long aTime, @NonNull final String aUserId,
                                                              @Nullable SensorStatus aSensorStatus) {
        final LogEntry.Algorithm algorithm = LogEntry.Algorithm.fromUserId(aUserId, aTime);
        // Get the database
        Database database = mMyApplication.acquireDatabase();
        try {
            PendingNewsEvent pendingNews = null;
            //find latest news not displayed
            if (algorithm == LogEntry.Algorithm.NONE) {
                //ignore sensor status
                pendingNews = database.getLatestPendingNewsEvent(aUserId, null);
                if (null == pendingNews) {
                    return null;
                }
                //but we need sensor status for logging
                if (null == aSensorStatus) {
                    aSensorStatus = mMyApplication.mEngine.getSensorStatus(aTime, aUserId);
                }
            } else {
                PendingNewsEvent pendingNewsEntry_text = database.getLatestPendingNewsEvent(aUserId, NewsEvent.Type
                        .TEXT);
                PendingNewsEvent pendingNewsEntry_picture = database.getLatestPendingNewsEvent(aUserId, NewsEvent.Type
                        .PICTURE);
                PendingNewsEvent pendingNewsEntry_video = database.getLatestPendingNewsEvent(aUserId, NewsEvent.Type
                        .VIDEO);

                if (null == pendingNewsEntry_text && null == pendingNewsEntry_picture && null ==
                        pendingNewsEntry_video) {
                    //fall back (maybe there is no Category in Word press)
                    pendingNews = database.getLatestPendingNewsEvent(aUserId, null);
                    if( null == pendingNews ) {
                        return null;
                    }
                }
                if (null == aSensorStatus) {
                    aSensorStatus = mMyApplication.mEngine.getSensorStatus(aTime, aUserId);
                }

                NewsEvent.Type preferredType = getPreferredNewsType(aSensorStatus, algorithm);
                if (null == preferredType) {
                    return null;
                }

                switch (preferredType) {
                    case VIDEO:
                        if (null == pendingNews) pendingNews = pendingNewsEntry_video;
                        //fall through
                    case PICTURE:
                        if (null == pendingNews) pendingNews = pendingNewsEntry_picture;
                        //fall through
                    case TEXT:
                        if (null == pendingNews) pendingNews = pendingNewsEntry_text;
                        break;
                }
            }

            if (null != pendingNews) {
                try {
                    database.deletePendingNewsEvent(pendingNews.rowId);
                } catch (DbEntryNotFoundException e) {
                    e.printStackTrace();
                }
                final long newsRowId = pendingNews.newsRowId;
                final NewsEvent newsEvent = database.getNewsEvent(newsRowId);
                if (null != newsEvent) return new MyApplication.NewsAndSensor(newsEvent, aSensorStatus);
            }
            return null;
        } finally {
            mMyApplication.releaseDatabase();
        }
    }//getNewsForNotification

}//class SmartNewsAlgorithm
//#############################################################################
//eof SmartNewsAlgorithm.java