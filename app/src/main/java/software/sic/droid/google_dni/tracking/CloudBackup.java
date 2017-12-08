/* www.sic.software
 *
 * @file CloudBackup.java
 * @date 2017-01-25
 * @brief this Class is used to send the Events to our Server in a BackgroundTask
 */
package software.sic.droid.google_dni.tracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import software.sic.droid.google_dni.BuildConfig;
import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.data.PendingLogEntry;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

public class CloudBackup extends IntentService {
    private static final String TAG = "CloudBackup";
    private final MyApplication mMyApplication = MyApplication.instance();
    private int mCountOK;
    private boolean mUserDataUploaded;

    public CloudBackup() {
        super(TAG);
    }//c'tor

    static public void startService(Context context) {
        if (DEBUG) {
            Log.v(TAG, "startWakefulService");
        }
        Intent service = new Intent(context, CloudBackup.class);
        WakefulBroadcastReceiver.startWakefulService(context, service);
    }//startService

    @Override
    protected void onHandleIntent(Intent intent) {
        if (DEBUG) {
            Log.i(TAG, "onHandleIntent");
        }
        mMyApplication.mStatistic.mCloudBackupCount++;
        mMyApplication.mStatus.mCloudBackupIsActive = true;
        mMyApplication.postUpdateUi();

        Database database = mMyApplication.acquireDatabase();
        try {
            doRun(database);
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }
        mMyApplication.releaseDatabase();

        if (DEBUG) {
            Log.i(TAG, "completeWakefulIntent mCountOK=" + mCountOK);
        }
        mMyApplication.mStatus.mCloudBackupIsActive = false;
        mMyApplication.postUpdateUi();
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }//onHandleIntent

    private void doUploadUserData() {
        SmartNewsSharedPreferences sp = SmartNewsSharedPreferences.instance();
        if (!sp.isUserInformationStoredAlready()) {
            return;
        }
        if (sp.isUserInformationUploadedAlready()) {
            mUserDataUploaded = true;
            return;
        }
        String logParam;
        try {
            logParam = "?uid=" + sp.getUserId()
                    + "&mail=" + URLEncoder.encode(sp.getEMail(), "utf-8")
                    + "&age=" + URLEncoder.encode(sp.getAge(), "utf-8")
                    + "&job=" + URLEncoder.encode(sp.getJob(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                Log.e(TAG, "???", e);
            }
            return;
        }

        try {
            URL url = new URL("http://test.test" + logParam);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");

            conn.connect();
            if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                sp.setUserInformationUploadedAlready();
                mUserDataUploaded = true;
            } else {
                if (DEBUG) {
                    Log.i(TAG, "users.php response=" + conn.getResponseCode());
                }
            }
        } catch (Exception ignore) {
            if (DEBUG) {
                Log.i(TAG, "users.php", ignore);
            }
        }
//        doLogToVH(logParam);
    }//doUploadUserData

    private void doRun(Database database) throws IOException {
        if (!mUserDataUploaded) {
            doUploadUserData();
        }
        mCountOK = 0;
        for (int retry = 0; retry < 10; ) {
            PendingLogEntry pendingLog = database.getPendingLogEntry();
            if (null == pendingLog) {
                return;
            }
            pendingLog.logEntry = database.getLogEntry(pendingLog.logEventRowId);
            if (null == pendingLog.logEntry) {
                database.deletePendingLogEntry(pendingLog);
                continue;
            }
            String logParam = "?p1=" + (pendingLog.logEntry.timestamp / 1000)//UTC seconds
                    + "&p2=" + pendingLog.logEntry.userId
                    + "&p3=" + pendingLog.logEntry.algorithm
                    + "&p4=" + pendingLog.logEntry.newsId
                    + "&p5=" + pendingLog.logEntry.newsType
                    + "&p6=" + pendingLog.logEntry.uiAction
                    + "&p7=" + pendingLog.logEntry.sensorStatus.activitiesStatus.detectedActivitiesState
                    + "&p8=" + pendingLog.logEntry.sensorStatus.interruptionFilterStatus.interruptionFilter
                    + "&p9=" + pendingLog.logEntry.sensorStatus.networkStatus.networkState
                    + "&p10=" + pendingLog.logEntry.sensorStatus.networkStatus.signalStrength
                    + "&p11=" + pendingLog.logEntry.sensorStatus.calendarStatus.currentEntries
                    + "&p12=" + pendingLog.logEntry.sensorStatus.calendarStatus.todayEntries
                    + "&p13=" + pendingLog.logEntry.sensorStatus.calendarStatus.previousStartTime
                    + "&p14=" + pendingLog.logEntry.sensorStatus.calendarStatus.previousEndTime
                    + "&p15=" + pendingLog.logEntry.sensorStatus.calendarStatus.nextStartTime
                    + "&p16=" + pendingLog.logEntry.sensorStatus.calendarStatus.nextEndTime
                    + "&p17=" + pendingLog.logEntry.detectedActivitiesAsString
                    + "&p18=" + pendingLog.logEntry.sensorStatus.displayStatus.isDisplayActive
                    + "&p19=" + BuildConfig.VERSION_CODE;

//            doLogToVH(logParam);
            URL url = new URL("http://gdnilog.sic.software.w0123d63.kasserver.com/" + logParam);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");

            try {
                conn.connect();
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // TODO: 06.04.17 - dg: Google analytics here

                    mCountOK++;
                    database.deletePendingLogEntry(pendingLog);
                    retry = 0;
                } else {
                    if (DEBUG) {
                        Log.i(TAG, "http response=" + responseCode);
                    }
                    return;
                }
            } catch (IOException e) {
                retry++;
                if (DEBUG) {
                    Log.w(TAG, "http retry=" + retry + " mCountOK=" + mCountOK + " :" + e);
                }
            }
        }
    }//doRun
}//class CloudBackup
//#############################################################################
//eof CloudBackup.java

