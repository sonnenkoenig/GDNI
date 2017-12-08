/*
 * www.sic.software
 *
 * @file SmartNewsService.java
 * @date 2017-02-27
 * @brief this Service polls Database and sensors in 5 minutes intervals
 */
package software.sic.droid.google_dni.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.util.SmartNewsSharedPreferences;

public class SmartNewsService extends IntentService {
    private static final String TAG = "SmartNewsService";
    final MyApplication mMyApplication = MyApplication.instance();

    public SmartNewsService() {
        super(TAG);
    }//c'tor

    static public void startService(Context context) {
        Intent service = new Intent(context, SmartNewsService.class);
        WakefulBroadcastReceiver.startWakefulService(context, service);
    }//startService

    static public void queueTimer(Context context) {
        final long interval = 5 * 60 * 1000;
//        Context context = mMyApplication.getApplicationContext();
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(SmartNewsService.class.getCanonicalName
                (), null, context, MyApplication.AlarmReceiver.class), 0);
        alarmMgr.cancel(pi);
        alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval,
                interval, pi);
    }//queueTimer

    @Override
    protected void onHandleIntent(Intent intent) {
        mMyApplication.mStatistic.mSmartNewsServiceCount++;
        mMyApplication.mStatus.mSmartNewsServiceIsActive = true;
        mMyApplication.postUpdateUi();

        try {
            //see #22669
            if(SmartNewsSharedPreferences.instance().isUserInformationStoredAlready()) {
                doRun();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        mMyApplication.mStatus.mSmartNewsServiceIsActive = false;
        mMyApplication.postUpdateUi();
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }//onHandleIntent

    private void doRun() {
        final MyApplication.NewsAndSensor newsAndSensor = mMyApplication.mSmartNewsAlgorithm.getNewsForNotification();
        if (null != newsAndSensor) {
            mMyApplication.showNotification(newsAndSensor);
        }
        Database db = mMyApplication.acquireDatabase();

        mMyApplication.mStatistic.mPendingNewsCount = db.getPendingNewsCount(SmartNewsSharedPreferences.instance().getUserId());
        mMyApplication.releaseDatabase();
    }//doRun

}//class SmartNewsService
//##################################################################################################
//eof SmartNewsService.java