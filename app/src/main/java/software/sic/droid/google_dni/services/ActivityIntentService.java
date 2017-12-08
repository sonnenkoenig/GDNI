/* www.sic.software
 * google-dni_smart_news
 *
 * @date Feb 2017
 */
package software.sic.droid.google_dni.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

import software.sic.droid.google_dni.MyApplication;

/**
 * Intent service which listens to updates for activities
 */
public class ActivityIntentService extends IntentService {
    // Log tag
    private static final String TAG = ActivityIntentService.class.getSimpleName();

    public ActivityIntentService() {
        super(TAG);
    }//c'tor

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get the result out of the intent
        ActivityRecognitionResult activityRecognitionResult = ActivityRecognitionResult.extractResult(intent);
        // Get the detected activities list out of the result
        List<DetectedActivity> detectedActivities = activityRecognitionResult
                .getProbableActivities();
        if (detectedActivities != null && detectedActivities.size() > 0) {
            final MyApplication myApp = MyApplication.instance();
            myApp.mEngine.onActivityState(detectedActivities);
        }
    }//onHandleIntent

    public enum DetectedActivityState {
        IN_VEHICLE, ON_BICYCLE, ON_FOOT, RUNNING, STILL, TILTING, UNKNOWN, WALKING;

        public static
        @Nullable
        DetectedActivityState valueOf(int a) {
            DetectedActivityState[] t = DetectedActivityState.values();
            if (a < 0 || a >= t.length) {
                return null;
            }
            return t[a];
        }//valueOf

    }//ActivityState

    public static class DetectedActivities {

        public int inVehicle;
        public int onBicycle;
        public int onFoot;
        public int running;
        public int still;
        public int tilting;
        public int unknown;
        public int walking;

        @Override
        public String toString() {
            return "" + inVehicle + "." + onBicycle + "." + onFoot + "." + running + "." + still + "." + tilting + "" +
                    "." + unknown + "." + walking;
        }
        public DetectedActivityState set(@NonNull List<DetectedActivity> aDetectedActivities){

            int maxConfidence = inVehicle = onBicycle = onFoot = running = still = tilting = unknown = walking = 0;
            DetectedActivityState maxActivity = null;
            for( DetectedActivity da : aDetectedActivities){
                final int confidence = da.getConfidence();
                DetectedActivityState ds;
                //!! attention DetectedActivity && DetectedActivityState have different ordinals!!
                switch (da.getType()) {
                    case DetectedActivity.IN_VEHICLE: inVehicle = confidence; ds = DetectedActivityState.IN_VEHICLE;break;
                    case DetectedActivity.ON_BICYCLE: onBicycle = confidence; ds = DetectedActivityState.ON_BICYCLE;break;
                    case DetectedActivity.ON_FOOT:    onFoot    = confidence; ds = DetectedActivityState.ON_FOOT;break;
                    case DetectedActivity.RUNNING:    running   = confidence; ds = DetectedActivityState.RUNNING;break;
                    case DetectedActivity.STILL:      still     = confidence; ds = DetectedActivityState.STILL;break;
                    case DetectedActivity.TILTING:    tilting   = confidence; ds = DetectedActivityState.TILTING;break;
                    case DetectedActivity.UNKNOWN:    unknown   = confidence; ds = DetectedActivityState.UNKNOWN;break;
                    case DetectedActivity.WALKING:    walking   = confidence; ds = DetectedActivityState.WALKING;break;
                    default:
                        continue;
                }
                if (maxConfidence < confidence) {
                    maxConfidence = confidence;
                    maxActivity = ds;
                }
            }
            return maxActivity;
        }//set

        @Override
        public boolean equals(Object obj) {
            if( null == obj){ return false;}
            if( obj instanceof DetectedActivities ){
                DetectedActivities o = (DetectedActivities)  obj;
                return o.inVehicle==inVehicle
                    && o.onBicycle==onBicycle
                    && o.onFoot==onFoot
                    && o.running==running
                    && o.still==still
                    && o.tilting==tilting
                    && o.unknown==unknown
                    && o.walking==walking
                    ;
            }
            return false;
        }
    }//DetectedActivities
}//ActivityIntentServiceRegistration
