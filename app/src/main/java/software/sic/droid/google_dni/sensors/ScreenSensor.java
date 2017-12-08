/*
 * www.sic.software
 *
 * @file ScreenSensor.java
 * @date 2017-01-25
 * @brief detect Screen On/Off events
 */
package software.sic.droid.google_dni.sensors;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;

import software.sic.droid.google_dni.data.data.SensorStatus;

public class ScreenSensor {

    /**
     * Gets the current state of the display, the display is either on or off
     *
     * @param context The context
     * @return Boolean flag if the display is on or not
     */
    public static boolean isDisplayActive(@NonNull Context context) {
        // Get the systems power manager
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        // Return the display state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        } else {
            //noinspection deprecation
            return powerManager.isScreenOn();
        }
    }//isDisplayActive

    /**
     * Gets the display status out of the database. The display status holds all the important information used in
     * the algorithm
     *
     * @return The display status for the given user
     */
    @NonNull
    public static SensorStatus.DisplayStatus getDisplayStatus(@NonNull Context context) {
        // Create the display status
        SensorStatus.DisplayStatus displayStatus = new SensorStatus.DisplayStatus();
        // If the cursor is not empty we set the information to the network status
        displayStatus.isDisplayActive = isDisplayActive(context);
        // Return the network status
        return displayStatus;
    }//getDisplayStatus
}//class ScreenSensor
//#############################################################################
//eof ScreenSensor.java

