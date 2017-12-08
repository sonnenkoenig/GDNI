/* www.sic.software
 *
 * @file DebugStatistic.java
 * @date 2017-01-25
 * @brief holds Statistic data for debug reasons
 */
package software.sic.droid.google_dni.data.data;

public class DebugStatistic {
    public int mRssFeedUpdateCount;
    public int mRssFeedPollCount;
    public int mNewsSimulationCount;
    public int mOnNewsEventCount;
    public int mCloudBackupCount;
    public int mSmartNewsServiceCount;
    public String mPendingNewsCount;
    public int mDatabaseAcquireCount;
    public int mDatabaseConnectionsCount;
    public int mActivityEventCount;
    public int mAlarmReceiverCount;

    @Override
    public String toString() {
        return "Statistic:"
              +"\n\t"+"mRssFeedUpdateCount="+mRssFeedPollCount+"."+mRssFeedUpdateCount
              +"\n\t"+"mNewsSimulationCount="+mNewsSimulationCount+"."+mOnNewsEventCount
              +"\n\t"+"mCloudBackupCount="+mCloudBackupCount
              +"\n\t"+"mSmartNewsServiceCount="+mSmartNewsServiceCount+";"+mPendingNewsCount
              +"\n\t"+"mDatabaseAcquireCount="+mDatabaseAcquireCount
              +"\n\t"+"mDatabaseConnectionsCount="+mDatabaseConnectionsCount
              +"\n\t"+"mActivityEventCount="+mActivityEventCount
              +"\n\t"+"mAlarmReceiverCount="+mAlarmReceiverCount
              +"\n" ;
    }//toString
}//class DebugStatistic
//#############################################################################
//eof DebugStatistic.java

