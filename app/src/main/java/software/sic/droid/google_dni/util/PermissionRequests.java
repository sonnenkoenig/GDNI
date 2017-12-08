/*
 * www.sic-software.com
 * google-dni_smart_news
 *
 * @date Feb 2017
 */
package software.sic.droid.google_dni.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

public class PermissionRequests {
    // Request code for multiple permissions
    private static final int MY_PERMISSIONS_REQUEST_REQUEST_MULTIPLE = 0;

    /**
     * Requesting all permissions we need for the app here.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static void checkAndRequestPermissions(@NonNull Activity activity) {
        // List to store the permissions to be requested in
        ArrayList<String> permissions = new ArrayList<>();
        // The parameter activity is the currently active activity
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {
            // The permission is not granted yet, add it to the to be requested list
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) != PackageManager
                .PERMISSION_GRANTED) {
            // The permission is not granted yet, add it to the to be requested list
            permissions.add(Manifest.permission.READ_CALENDAR);
        }
        if (permissions.size() > 0) {
            // Request all permissions not granted yet
            ActivityCompat.requestPermissions(activity, permissions.toArray(new String[permissions.size()]),
                    MY_PERMISSIONS_REQUEST_REQUEST_MULTIPLE);
        }
    }//checkAndRequestPermissions

    /**
     * Handle the permission results (aka user accepts or declines the permissions)
     *
     * @param requestCode  The request code this will be 0 cause we just request multiple permissions at once
     * @param permissions  List which contains all permissions request
     * @param grantResults List which contains the result of the permission request this is either granted or denied
     */
    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        // We handle the error which occurs when the user declined the permissions by ourselves so we can ignore this
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_REQUEST_MULTIPLE:
                for (int i = 0; i < permissions.length; i++) {
                    //noinspection StatementWithEmptyBody
                    if (grantResults.length > i && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        // Permission was granted
                    } else {
                        // Permission was declined
                    }
                }
                break;
            default:
                break;
        }
    }//onRequestPermissionsResult
}//PermissionRequests
