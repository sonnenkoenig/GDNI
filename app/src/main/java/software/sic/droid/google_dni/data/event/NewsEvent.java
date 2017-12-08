/*
 * www.sic.software
 *
 * @file NewsEvent.java
 * @date 2017-01-25
 * @brief news received from RssFeed
 */
package software.sic.droid.google_dni.data.event;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.UUID;

import software.sic.droid.google_dni.R;

import static software.sic.droid.google_dni.simulation.UserSimulator.doGetRandomEnum;

public class NewsEvent implements Serializable {
    public long rowId;//DB row see SQLiteDatabase.insert
    public long timeStamp;//System.millis - news reported/detected?
    public String id;
    public String title;
    public String summary;
    public String content;
    @Nullable
    public Type type;

    public static NewsEvent createDummyNews( Type aType, @NonNull Context context) {
        NewsEvent e = new NewsEvent();
        e.id = UUID.randomUUID().toString();
        e.timeStamp = System.currentTimeMillis();

        if(aType == null) {
            e.type = Type.VIDEO;
            e.title = context.getString(R.string.STR_DEBUG_NEWS_TITLE);
            e.summary = context.getString(R.string.STR_DEBUG_NEWS_SUMMARY);
            //e.content = context.getString(R.string.STR_DEBUG_NEWS_CONTENT);
            return e;
        }

        switch(e.type = aType){
            case VIDEO:
                e.title = context.getString(R.string.STR_DEBUG_VIDEO_NEWS_TITLE);
                e.summary = context.getString(R.string.STR_DEBUG_VIDEO_NEWS_SUMMARY);
                e.content = "<p>Ein Testvideo</p>" + "<p>dummyContent " + e.id + " " + e.type + "<p/>" + context
                        .getString(R.string.STR_DEBUG_VIDEO_NEWS_CONTENT);
                break;
            case PICTURE:
                e.title = context.getString(R.string.STR_DEBUG_PICTURE_NEWS_TITLE);
                e.summary = context.getString(R.string.STR_DEBUG_PICTURE_NEWS_SUMMARY);
                e.content = context.getString(R.string.STR_DEBUG_PICTURE_NEWS_CONTENT) + "<p>" + e.id + " " + e
                        .type + "</p>";
                break;
            case TEXT:
                e.title = context.getString(R.string.STR_DEBUG_TEXT_NEWS_TITLE);
                e.summary = context.getString(R.string.STR_DEBUG_TEXT_NEWS_SUMMARY);
                e.content = context.getString(R.string.STR_DEBUG_TEXT_NEWS_CONTENT)+"<br/>"+ e.id
                        + "<br/>" + e.type;
                break;
            default:
                e.title = "!dummyTitle " + e.id;
                e.summary = "!dummySummary " + e.id;
                e.content = "dummyContent " + e.id +"<br/>"+e.type;
                break;
        }
        return e;
    }//createDummyNews


    public static NewsEvent createDummyNews(@NonNull Context context) {
        return createDummyNews( doGetRandomEnum(Type.values()), context);
    }//createDummyNews

    public enum Type {TEXT, PICTURE, VIDEO;

        public static @Nullable Type valueOf(int a){
            Type[] t = Type.values();
            if(a<0||a>=t.length){return null;}
            return t[a];
        }//valueOf
    }//enum Type

    private NewsEvent() {
        this.id = "";
        this.timeStamp = 0;
        this.title = "";
        this.summary = "";
        this.content = "";
        this.type = Type.TEXT;
    }

    public NewsEvent(long rowid, long timeStamp, String id,
                     String title, String summary, String content,  int type) {
        this.rowId = rowid;
        this.timeStamp = timeStamp;
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;

        this.type = Type.valueOf(type);
    }
}//class NewsEvent
//#############################################################################
//eof NewsEvent.java
