/* www.sic.software
 *
 * @file NewsSimulator.java
 * @date 2017-01-25
 * @brief we are using AlarmTimer to Simulate a news event every 5'
 */
package software.sic.droid.google_dni.simulation;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.event.NewsEvent;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;

public final class NewsSimulator extends IntentService {
    private static final String TAG = "NewsSimulator";
    private final MyApplication mMyApplication = MyApplication.instance();
    private final UserSimulator mUserSimulator = new UserSimulator();

    public NewsSimulator() {
        super(TAG);
    }//c'tor

    static public void queueTimer(Context context) {
        final long interval = 5 * 60 * 1000;
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(NewsSimulator.class.getCanonicalName(),
                null, context, MyApplication.AlarmReceiver.class), 0);
        alarmMgr.cancel(pi);
        if(DEBUG){ Log.d(TAG,"queueTimer="+MyApplication.instance().mStatus.mNewsSimulationIsActivated);}
        if(MyApplication.instance().mStatus.mNewsSimulationIsActivated) {
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, interval, pi);
        }
    }//queueTimer


    static public void startService(Context context) {
        Intent service = new Intent(context, NewsSimulator.class);
        WakefulBroadcastReceiver.startWakefulService(context, service);
    }//startService

    @Override
    protected void onHandleIntent(Intent intent) {
        mMyApplication.mStatistic.mNewsSimulationCount++;

        mMyApplication.mStatus.mNewsSimulationIsActive = true;
        mMyApplication.postUpdateUi();

        if(MyApplication.instance().mStatus.mNewsSimulationIsActivated) {
            final NewsEvent dummyNews = NewsEvent.createDummyNews(this);
            mMyApplication.mEngine.onNewsEvent(dummyNews);
            mUserSimulator.onSimulatedNews(dummyNews);
        }

        mMyApplication.mStatus.mNewsSimulationIsActive = false;
        mMyApplication.postUpdateUi();
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }//onHandleIntent

}//class NewsSimulator
//#############################################################################
//eof NewsSimulator.java
