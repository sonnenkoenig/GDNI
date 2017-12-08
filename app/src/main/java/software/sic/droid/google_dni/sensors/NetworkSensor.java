/*
 * www.sic.software
 *
 * @file NetworkSensor.java
 * @date 2017-01-25
 * @brief detect changes in Network connection
 */
package software.sic.droid.google_dni.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.List;

import software.sic.droid.google_dni.MyApplication;
import software.sic.droid.google_dni.data.Database;
import software.sic.droid.google_dni.data.data.SensorStatus;

import static software.sic.droid.google_dni.BuildConfig.DEBUG;


public class NetworkSensor {
    // Constant for the signal strength
    private static final int SIGNAL_STRENGTH_LEVELS = 5;
    private final static String TAG = "NetworkSensor";

    /**
     * Gets the current network state of the device the constants fot the type are defined in the connectivity manager
     *
     * @param context The context
     * @return Type of the current network state
     */
    private static NetworkState getNetworkState(@NonNull Context context) {
        // Get the systems connectivity manager
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        // Get the current network information
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return NetworkState.NONE;
        }

        // Return the network type
        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return NetworkState.WIFI;
            case ConnectivityManager.TYPE_MOBILE:
                // Get the systems telephony manager
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context
                        .TELEPHONY_SERVICE);
                final int networkType;
                try{
                    networkType = telephonyManager.getNetworkType();
                }catch (Throwable e){
                    if(DEBUG){Log.w(TAG,"can't getMobileNetworkType",e);}
                    return NetworkState.NONE;
                }
                // Switch over the network types
                switch (networkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return NetworkState.TWO_G;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return NetworkState.THREE_G;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return NetworkState.FOUR_G;
                    default:
                        return NetworkState.NONE;
                }
            default:
                return NetworkState.NONE;
        }
    }//getNetworkStatus

    /**
     * Gets the signal strength of the wifi signal
     *
     * @param context The context
     * @return Signal strength in the range between 0 and _SIGNAL_STRENGTH_RANGE_MAX - 1 (both included)
     */
    private static int getWifiSignalStrength(@NonNull Context context) {
        // Get the systems wifi manager
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context
                .WIFI_SERVICE);
        // Get the current wifi info
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        // Return the signal strength of the current wifi signal
        return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), SIGNAL_STRENGTH_LEVELS);
    }//getWifiSignalStrength

    /**
     * Gets the signal strength of the mobile data of the device
     *
     * @param context The context
     * @return The level of the mobile signal strength in the range between 0 and 4 (both included)
     */
    private static int getMobileSignalStrength(@NonNull Context context) {
        // Get the systems telephony manager
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int strength = -1;
        // Get the list of all mobile cells
        final List<CellInfo> cellInfoList;
        try{
            cellInfoList = telephonyManager.getAllCellInfo();
        }catch (Throwable e){
            if(DEBUG){Log.w(TAG,"can't get getMobileSignalStrength",e);}
            return strength;
        }
        if (cellInfoList == null || cellInfoList.size() == 0) {
            return strength;
        }

        final CellInfo cellInfo = cellInfoList.get(0);

        if (cellInfo instanceof CellInfoLte) {
            // Get the cell info of the current mobile connection
            CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
            // Get the signal strength of the current mobile connection
            CellSignalStrengthLte cellSignalStrength = cellInfoLte.getCellSignalStrength();
            // Return the level of the signal strength
            strength = cellSignalStrength.getLevel();
        } else if (cellInfo instanceof CellInfoGsm) {
            // Get the cell info of the current mobile connection
            CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
            // Get the signal strength of the current mobile connection
            CellSignalStrengthGsm cellSignalStrength = cellInfoGsm.getCellSignalStrength();
            // Return the level of the signal strength
            strength = cellSignalStrength.getLevel();
        } else if (cellInfo instanceof CellInfoWcdma) {
            // Get the cell info of the current mobile connection
            CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
            // Get the signal strength of the current mobile connection
            CellSignalStrengthWcdma cellSignalStrength = cellInfoWcdma.getCellSignalStrength();
            // Return the level of the signal strength
            strength = cellSignalStrength.getLevel();
        } else if (cellInfo instanceof CellInfoCdma) {
            // Get the cell info of the current mobile connection
            CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
            // Get the signal strength of the current mobile connection
            CellSignalStrengthCdma cellSignalStrength = cellInfoCdma.getCellSignalStrength();
            // Return the level of the signal strength
            strength = cellSignalStrength.getLevel();
        }

        return strength;
    }//getMobileSignalStrength

    /**
     * @return network status from System API
     */
    @NonNull
    public static SensorStatus.NetworkStatus getNetworkStatus(@NonNull final Context context) {
        // Create the network status
        SensorStatus.NetworkStatus networkStatus = new SensorStatus.NetworkStatus();
        networkStatus.signalStrength = -1;
        //workaround #24594 - ungeklärte Abstürze
        try {
            // Set the fields to the network status
            networkStatus.networkState = getNetworkState(context);
        }catch(Throwable e){
            if(DEBUG){Log.e(TAG,"can't getNetworkStatus",e);}
        }
        if (networkStatus.networkState != null) {
            switch (networkStatus.networkState) {
                case WIFI:
                    networkStatus.signalStrength = getWifiSignalStrength(context);
                    break;
                case FOUR_G:
                case THREE_G:
                case TWO_G:
                    networkStatus.signalStrength = getMobileSignalStrength(context);
                    break;
            }
        }else{
            networkStatus.networkState = NetworkState.NONE;
        }
        // Return the network status
        return networkStatus;
    }//getNetworkStatus

    /**
     * Enum to describe the network state
     */
    public enum NetworkState {
        NONE, WIFI, TWO_G, THREE_G, FOUR_G;

        public static
        @Nullable
        NetworkState valueOf(int a) {
            NetworkState[] t = NetworkState.values();
            if (a < 0 || a >= t.length) {
                return null;
            }
            return t[a];
        }//valueOf
    }

    /**
     * Broadcast receiver for the network state. If the network state changes this class gets notified about it
     */
    public static class NetworkStateChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the application
            final MyApplication myApp = MyApplication.instance();
            // Get the database
            Database database = myApp.acquireDatabase();
            // Save all entries in the database
            database.createNetworkState(NetworkSensor.getNetworkStatus(myApp));
            // Close the database after all action are completed to prevent leaks
            myApp.releaseDatabase();
        }//onReceive
    }//NetworkStateChangedReceiver
}//class NetworkSensor
//#############################################################################
//eof NetworkSensor.java

