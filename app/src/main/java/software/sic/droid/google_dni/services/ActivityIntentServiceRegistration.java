/*
 * sic.software
 * google-dni_smart_news
 *
 * @date Feb 2017
 */
package software.sic.droid.google_dni.services;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

/**
 * Class to register to get the activities of the device. Activities can be IN_VEHICLE, ON_BICYCLE, ON_FOOT, RUNNING,
 * STILL, TILTING, UNKNOWN, WALKING and have a confidence between 0 and 100
 */
public class ActivityIntentServiceRegistration implements ConnectionCallbacks, OnConnectionFailedListener,
        ResultCallback<Status> {
    // The context
    private final Context mContext;
    // The google api client which get the activities requested from
    private GoogleApiClient mGoogleApiClient;

    public ActivityIntentServiceRegistration(@NonNull Context context) {
        // Save the context for later use
        this.mContext = context;
        // Initialize the google api client
        buildGoogleApiClient();
        // Connect with the google api client
        mGoogleApiClient.connect();
    }

    /**
     * Builds an google api client with the activity recognition api and sets the connection and the connection failed
     * listener
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this.mContext).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(ActivityRecognition.API).build();
    }//buildGoogleApiClient

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Request the activities as soon as the google api client is connected
        requestActivities();
    }//onConnected

    @Override
    public void onConnectionSuspended(int i) {
    }//onConnectionSuspended

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }//onConnectionFailed

    @Override
    public void onResult(@NonNull Status status) {
    }//onResult

    /**
     * Requests the current device activity from the google api client and registers itself as listener for the results
     */
    private void requestActivities() {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 0,
                getActivityDetectionPendingIntent()).setResultCallback(this);
    }//requestActivities

    /**
     * Creates a pending intent which will be the callback for the incoming activities
     *
     * @return ActivityIntentService to handle the incoming activities
     */
    @NonNull
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this.mContext, ActivityIntentService.class);
        return PendingIntent.getService(this.mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }//getActivityDetectionPendingIntent
}//class ActivityIntentService
//#############################################################################
//eof ActivityIntentService.java

