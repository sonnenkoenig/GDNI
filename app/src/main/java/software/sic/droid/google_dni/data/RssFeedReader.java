/*
 * www.sic.software
 *
 * @file RssFeedReader.java
 * @date 2017-01-25
 * @brief collect new News from RssFeed
 */
package software.sic.droid.google_dni.data;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.event.NewsEvent;
import software.sic.droid.google_dni.sensors.NetworkSensor;
import software.sic.droid.google_dni.services.SmartNewsService;

public class RssFeedReader extends IntentService {
    private static final String TAG = "RssFeedReader";
    private final MyApplication mMyApplication = MyApplication.instance();

    public RssFeedReader() {
        super(TAG);
    }//c'tor
    static public final int TURBO_POLLING_INTERVAL_MILLIS = 30*1000;

    static public void queueTimer(Context context) {
        final long interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(RssFeedReader.class.getCanonicalName(),
                null, context, MyApplication.AlarmReceiver.class), 0);
        alarmMgr.cancel(pi);
        if(MyApplication.instance().mStatus.mTurboPollingIsActivated) {
            alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+TURBO_POLLING_INTERVAL_MILLIS, pi);
        }else{
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + interval, interval, pi);
        }
    }//queueTimer

    static public void startService(Context context) {
        Intent service = new Intent(context, RssFeedReader.class);
        WakefulBroadcastReceiver.startWakefulService(context, service);
    }//startService

    @Override
    protected void onHandleIntent(Intent intent) {
        mMyApplication.mStatistic.mRssFeedPollCount++;
        mMyApplication.mStatus.mRssFeedUpdateIsActive = true;
        mMyApplication.postUpdateUi();

        List<NewsEvent> feed = null;
        RssFeedParser mRssParser = new RssFeedParser();
        if (NetworkSensor.getNetworkStatus(mMyApplication).networkState != NetworkSensor.NetworkState.NONE) {
            try {
                feed = mRssParser.parseFeed();
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
        }

        //if (feed == null) all news are up to date
        if (feed != null) {
            mMyApplication.mStatistic.mRssFeedUpdateCount++;
            for (int i = feed.size() - 1; i >= 0; i--) {
                mMyApplication.mEngine.onNewsEvent(feed.get(i));
            }
            SmartNewsService.startService(this);
        }

        mMyApplication.mStatus.mRssFeedUpdateIsActive = false;
        mMyApplication.postUpdateUi();
        if(MyApplication.instance().mStatus.mTurboPollingIsActivated) {
            AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(RssFeedReader.class.getCanonicalName(),null, this, MyApplication.AlarmReceiver.class), 0);
            alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+ TURBO_POLLING_INTERVAL_MILLIS, pi);
        }

        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }//onHandleIntent
}//class RssFeedReader
//#############################################################################
//eof RssFeedReader.java
