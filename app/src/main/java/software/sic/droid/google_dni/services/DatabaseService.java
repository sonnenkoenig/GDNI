/*
 * www.sic.software
 *
 * @file DatabaseService.java
 * @date 2017-03-10
 * @brief helper to get database closed as late as possible
 */
package software.sic.droid.google_dni.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class DatabaseService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}//class DatabaseService
//#############################################################################
//eof DatabaseService.java

