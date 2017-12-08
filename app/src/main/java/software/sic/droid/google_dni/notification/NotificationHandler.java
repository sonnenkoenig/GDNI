/*
 * www.sic-software.com
 * google-dni_smart_news
 *
 * @date Feb 2017
 */
package software.sic.droid.google_dni.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.R;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.data.event.UiEvent;
import software.sic.droid.google_dni.ui.activities.WebViewActivity;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

/**
 * Notification handler class which handles the creation and the display of the notifications
 */
public class NotificationHandler {

    // Identifier for the news event, used to identify the news event passed as intent extra
    public static final String NEWS_EVENT = "news_event";
    // Identifier for the delete action of the notification, used by the broadcast receiver to identify the action
    private static final String DELETE_INTENT_ACTION = "delete_intent_action";
    private static final NotificationHandler sInstance = new NotificationHandler();

    @NonNull
    public static NotificationHandler instance() {
        return sInstance;
    }//instance

    /**
     * Method to show a notification to the user. This method is the connection to the outside of this class. Keep
     * the access to the this class simple
     *
     * @param context The context for the notification
     */
    public void showNotification(@NonNull Context context, @NonNull NewsEvent newsEvent) {
        // Post the notification in the notification center
        this.postNotification(context, buildNotification(context, newsEvent));
    }//showNotification

    /**
     * Builds the notification with all the required information to display to the user
     *
     * @param context Context needed to create the notification builder
     * @return The notification builder with all the notification info needed
     */
    @NonNull
    private NotificationCompat.Builder buildNotification(@NonNull Context context, @NonNull NewsEvent newsEvent) {
        // Create the notification builder
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        // REQUIRED
        // Small icon which is shown in the status bar
        notificationBuilder.setSmallIcon(R.drawable.ic_fiber_new_white_24dp);
        // Title of the notification
        notificationBuilder.setContentTitle(newsEvent.title);
        // Text of the notification
        notificationBuilder.setContentText(newsEvent.summary);
        // OPTIONAL
        // Set so the notification cancels itself when pressed
        notificationBuilder.setAutoCancel(true);
        // Large icon shown in the ticker and in the notification
        notificationBuilder.setLargeIcon(getBitmapForNewsEventType(context, newsEvent));
        // Text shown when the notification first arrives
        notificationBuilder.setTicker(context.getString(R.string.STR_TICKER_NOTIFICATION_TEXT));
        // Intent for when the user presses the notification banner
        notificationBuilder.setContentIntent(getContentIntent(context, newsEvent));
        // Intent for when the user swipes away the notification or uses the "clear all" function for notifications
        notificationBuilder.setDeleteIntent(getDeleteIntent(context, newsEvent));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(Notification.CATEGORY_RECOMMENDATION);
        }
        //set sound and vibration
        // on notification
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        notificationBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        notificationBuilder.setLights(Color.RED, 3000, 3000);
        // Return the notification builder
        return notificationBuilder;
    }//buildNotification

    /**
     * Gets the right icon for the news type as bitmap
     *
     * @param context   The context
     * @param newsEvent The news event for what the icon is needed
     * @return Bitmap of the icon depending on the type of the news event
     */
    @NonNull
    private Bitmap getBitmapForNewsEventType(@NonNull Context context, @NonNull NewsEvent newsEvent) {
        // Initialize the resource id with the icon for text
        int resourceId = R.drawable.ic_subject_black_48dp_smartnews;
        // If the news event type is set switch over the type and set the matching icon as resource id
        if (newsEvent.type != null) {
            switch (newsEvent.type) {
                case PICTURE:
                    resourceId = R.drawable.ic_photo_black_48dp_smartnews;
                    break;
                case VIDEO:
                    resourceId = R.drawable.ic_play_circle_filled_black_48dp_smartnews;
                    break;
            }
        }
        // Return the icon as bitmap
        return BitmapFactory.decodeResource(context.getResources(), resourceId);
    }//getBitmapForNewsEventType

    /**
     * Posts the notification so the user can see it in the notification center
     *
     * @param context             The context for the notification
     * @param notificationBuilder The notification builder which contains the notification itself along with
     *                            important information
     */
    private void postNotification(@NonNull Context context, @NonNull NotificationCompat.Builder notificationBuilder) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);
        // ID for the notification
        int notificationId = 0;//(int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }//postNotification

    /**
     * Creates the content intent for the notification. The content intent will get called when the user taps on the
     * notification
     *
     * @return Intent for when the notification gets pressed
     */
    @NonNull
    private PendingIntent getContentIntent(@NonNull Context context, @NonNull NewsEvent newsEvent) {
        // Create an intent containing information about the news
        Intent intent = new Intent(context, WebViewActivity.class);
        // Set the news id to the intent
        intent.putExtra(NEWS_EVENT, newsEvent);
        // Return the pending intent for the press on a notification
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }//getContentIntent

    /**
     * Creates the content intent for the notification. The content intent will get called when the user swipes away
     * the notification or when the clear all button is pressed
     *
     * @return Intent for when the notification swiped away or when the clear all button is pressed
     */
    @NonNull
    private PendingIntent getDeleteIntent(@NonNull Context context, @NonNull NewsEvent newsEvent) {
        // Create an intent containing information about the news
        Intent intent = new Intent(DELETE_INTENT_ACTION);
        // Set the news id to the intent
        intent.putExtra(NEWS_EVENT, newsEvent);
        // Return the pending intent for the deletion of an notification
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }//getDeleteIntent

    /**
     * Notification broadcast receiver which listens to user interaction on the notification
     */
    public static class NotificationBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the news id out of the intent
            NewsEvent newsEvent = (NewsEvent) intent.getSerializableExtra(NEWS_EVENT);
            if (newsEvent != null && newsEvent.id != null) {
                // Get the database
                final MyApplication myApp = MyApplication.instance();
                switch (intent.getAction()) {
                    case DELETE_INTENT_ACTION:
                        UiEvent uiEvent = new UiEvent(newsEvent, SmartNewsSharedPreferences.instance().getUserId(),
                                UiEvent.Action.SWIPED);
                        myApp.mEngine.onUiEvent(uiEvent);
                        break;
                    default:
                        break;
                }
            }
        }//onReceive
    }//NotificationBroadcastReceiver
}//NotificationHandler
