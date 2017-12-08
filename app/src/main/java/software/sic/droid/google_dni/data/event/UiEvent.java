/*
 * www.sic.software
 *
 * @file UiEvent.java
 * @date 2017-01-25
 * @brief event generated if a user interact with a news?
 */
package software.sic.droid.google_dni.data.event;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import software.sic.droid.google_dni.data.data.LogEntry;

public class UiEvent {
    public final long timeStamp;//System.millis
    public final NewsEvent newsEvent;
    public final Action action;
    public final String userId;
    public final LogEntry.Algorithm algorithm;

    public UiEvent(@NonNull NewsEvent aNewsEvent, @NonNull String aUserId, @NonNull Action aAtion) {
        timeStamp = System.currentTimeMillis();
        newsEvent = aNewsEvent;
        userId = aUserId;
        action = aAtion;
        algorithm = LogEntry.Algorithm.fromUserId(aUserId,timeStamp);
    }//c'tor

    public enum Action {NOTIFIED, SWIPED, REPLACED, SHOWN, CLOSED, VIDEO_PLAY, VIDEO_FINISHED, RECEIVED, SHOWN_LAST;
        public static @Nullable Action valueOf(int a){
            Action[] t = Action.values();
            if(a<0||a>=t.length){return null;}
            return t[a];
        }//valueOf
    }
}//class UiEvent
//#############################################################################
//eof UiEvent.java

