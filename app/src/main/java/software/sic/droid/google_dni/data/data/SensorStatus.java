/* www.sic.software
 *
 * @file SensorStatus.java
 * @date 2017-03-21
 * @brief represents current status of all Sensors
*/
package software.sic.droid.google_dni.data.data;


import java.util.Date;

import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.sensors.NetworkSensor;
import software.sic.droid.google_dni.services.ActivityIntentService;
import software.sic.droid.google_dni.services.InterruptionFilterIntentService;

public class SensorStatus {
    public ActivitiesStatus activitiesStatus;
    public CalendarStatus calendarStatus;
    public DisplayStatus displayStatus;
    public InterruptionFilterStatus interruptionFilterStatus;
    public NetworkStatus networkStatus;

    @Override
    public String toString() {
        return "Sensors:"
                + "\n\t" + "activitiesStatus=" + activitiesStatus
                + "\n\t" + "calendarStatus=" + calendarStatus
                + "\n\t" + "displayStatus=" + displayStatus
                + "\n\t" + "interruptionFilterStatus=" + interruptionFilterStatus
                + "\n\t" + "networkStatus=" + networkStatus
                + "\n";
    }//toString

    public static class ActivitiesStatus {
        public ActivityIntentService.DetectedActivityState detectedActivitiesState;
        public ActivityIntentService.DetectedActivities detectedActivities;
        public long timeStamp;

        @Override
        public String toString() {
            return "\n\t\t" + "detectedActivities=" + detectedActivities+ " - "+detectedActivitiesState
            +"\n\t\t" + Database.sSimpleDateFormat.format(new Date(timeStamp));
        }
    }//ActivitiesStatus

    public static class CalendarStatus {
        public long previousStartTime;
        public long previousEndTime;
        public long nextStartTime;
        public long nextEndTime;
        public int currentEntries;
        public int todayEntries;

        @Override
        public String toString() {
            return "\n\t\t" + "entries=" + currentEntries + "." + todayEntries
                    + "\n\t\t" + "prev=" + previousStartTime + "." + (previousEndTime - previousStartTime)
                    + "\n\t\t" + "next=" + nextStartTime + "." + (nextEndTime - nextStartTime);
        }
    }//CalendarStatus

    public static class DisplayStatus {
        public boolean isDisplayActive;

        @Override
        public String toString() {
            return "\n\t\t" + "isDisplayActive=" + isDisplayActive;
        }
    }//DisplayStatus

    public static class InterruptionFilterStatus {
        public InterruptionFilterIntentService.InterruptionFilters interruptionFilter;

        @Override
        public String toString() {
            return "\n\t\t" + "interruptionFilter=" + interruptionFilter;
        }
    }//InterruptionFilterStatus

    public static class NetworkStatus {
        public NetworkSensor.NetworkState networkState;
        public int signalStrength;

        @Override
        public String toString() {
            return "\n\t\t" + "networkState=" + networkState + " " + signalStrength +"%";
        }
    }//NetworkStatus
}//class SensorStatus
//#############################################################################
//eof SensorStatus.java
