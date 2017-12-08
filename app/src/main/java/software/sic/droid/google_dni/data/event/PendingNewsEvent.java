package software.sic.droid.google_dni.data.event;

import java.io.Serializable;

/**
 * www.sic.software
 *
 * 17.03.17
 */
public class PendingNewsEvent implements Serializable {
    public long rowId;
    public long newsRowId;
    public NewsEvent.Type type;
    public String userUuid;

    public PendingNewsEvent(long rowId, String userUuid, long newsRowId, int type) {
        this.rowId = rowId;
        this.userUuid = userUuid;
        this.newsRowId = newsRowId;

        this.type = NewsEvent.Type.valueOf(type);
    }

    @Override
    public String toString() {
        return "pending newsRowId="+newsRowId+" t="+type;
    }//toString
}//class PendingNewsEvent
