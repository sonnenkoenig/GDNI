/* www.sic.software
 *
 * @file DebugAppStatus.java
 * @date 2017-01-25
 * @brief represents current status of app (primary for debug reasons)
 */
package software.sic.droid.google_dni.data.data;

import java.util.Date;

import software.sic.droid.google_dni.data.Database;

public class DebugAppStatus {
    public boolean mRssFeedUpdateIsActive;
    public boolean mNewsSimulationIsActive;
    public boolean mCloudBackupIsActive;
    public boolean mSmartNewsServiceIsActive;
    public String mUserId;
    public boolean mUserSimulationIsActivated;
    public boolean mNewsSimulationIsActivated;
    public boolean mTurboPollingIsActivated;
    private final String mCtorTime = Database.sSimpleDateFormat.format(new Date());
    public boolean mBootDetected;

    @Override
    public String toString() {
        return "Status:"
                +"\n\t"+"mRssFeedUpdateIsActive="+mRssFeedUpdateIsActive
                +"\n\t"+"mNewsSimulationIsActive="+mNewsSimulationIsActive
                +"\n\t"+"mCloudBackupIsActive="+mCloudBackupIsActive
                +"\n\t"+"mSmartNewsServiceIsActive="+mSmartNewsServiceIsActive
                +"\n\t"+"mUserId="+mUserId
                +"\n\t"+"alg="+LogEntry.Algorithm.fromUserId(mUserId)
                +"\n\t"+"simulation User="+mUserSimulationIsActivated+" News="+mNewsSimulationIsActivated
                +"\n\t"+"mTurboPollingIsActivated="+mTurboPollingIsActivated
                +"\n\t"+"mC'tor Time="+mCtorTime
                +"\n\t"+"mBootDetected="+mBootDetected
                +"\n" ;
    }//toString
}//class DebugAppStatus
//#############################################################################
//eof DebugAppStatus.java

