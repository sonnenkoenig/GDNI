package software.sic.droid.google_dni.data.data;

/*
 * www.sic.software
 *
 * 17.03.17
 */

import android.support.annotation.NonNull;

/**
 * Calendar event to represent entries from the calendar
 */
public class CalendarEvent {
    private final String mUserID;
    private final long mId;
    private final long mStartDate;
    private final long mEndDate;
    private final boolean mIsAllDay;

    public CalendarEvent(@NonNull String aUserID, long id, long startTime, long endTime, boolean isAllDay) {
        this.mUserID = aUserID;
        this.mId = id;
        this.mStartDate = startTime;
        this.mEndDate = endTime;
        this.mIsAllDay = isAllDay;
    }

    public String getUserID() {
        return mUserID;
    }//getUserID

    public long getId() {
        return mId;
    }//getId

    public boolean getIsAllDay() {
        return mIsAllDay;
    }//getIsAllDay

    public long getStartDate() {
        return mStartDate;
    }//getStartDate

    public long getEndDate() {
        return mEndDate;
    }//getEndDate

}//CalendarEvent