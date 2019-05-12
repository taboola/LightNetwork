package com.taboola.lightnetwork;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import com.taboola.lightnetwork.utils.PermissionUtils;

/**
 * This class is meant to provide utility information relevant for networking operations.
 *
 * NOTE: Using State methods usually requires the Permission(Manifest.permission.ACCESS_NETWORK_STATE).
 */
public class State {
    private static final String TAG = State.class.getSimpleName();
    private final Context mContext;

    public State(Context context){
        mContext = context;
    }

    /******************
     * Public methods *
     ******************/

    /**
     * @return - True if network is currently connected, false otherwise.
     */
    public boolean isConnected() {
        if (android.os.Build.VERSION.SDK_INT >= 28) { //28 is Q, const unavailable yet.
            return isConnected_Q();
        } else {
            return isConnected_preQ();
        }
    }

    /**
     * @return - True if current connection is Wifi, false otherwise.
     */
    public boolean isConnectedThroughWifi() {
        if (android.os.Build.VERSION.SDK_INT >= 28) { //28 is Q, const unavailable yet.
            return isConnectedThroughWifi_Q();
        } else {
            return isConnectedThroughWifi_preQ();
        }
    }

    /************************
     * Private Main Methods *
     ************************/

    /**
     * Sources:
     *      Deprecation: https://developer.android.com/reference/android/net/NetworkInfo
     *      New Check: https://developer.android.com/reference/android/net/NetworkCapabilities.html#hasCapability(int)
     */
    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private boolean isConnected_Q() {
        //Get ConnectivityManager.
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Check current network has Internet access.
        if (connectivityManager != null) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) { //No default network is currently active.
                return false;
            }

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (networkCapabilities != null && isConnectionValidated(networkCapabilities)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Is device connected to the Internet.
     *
     * Warning: this method requires Android Permission not asked in core SDK.
     * Use only if you're sure you have such permission.
     *
     * @return True if device is connected to the Internet. False if not connected or failed to query connectivity.
     */
    private boolean isConnected_preQ() {
        if (checkNetworkStatePermission()) return false;

        NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Sources:
     *      Deprecation: https://developer.android.com/reference/android/net/NetworkInfo
     *      New Check: https://developer.android.com/reference/android/net/NetworkCapabilities.html#hasCapability(int)
     */
    @TargetApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    private boolean isConnectedThroughWifi_Q() {
        //Get ConnectivityManager.
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Check WIFI capability in currently active network.
        if (connectivityManager != null) {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) { //No default network is currently active.
                return false;
            }

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (networkCapabilities != null && isConnectionValidated(networkCapabilities) && isWifiTypeConnection(networkCapabilities)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Is device connected to the Internet using WiFi.
     *
     * Warning: this method requires Android Permission not asked in core SDK.
     * Use only if you're sure you have such permission.
     *
     * @return True if device is connected to the Internet using WiFi. False if not connected or failed to query connectivity.
     */
    private boolean isConnectedThroughWifi_preQ() {
        if (checkNetworkStatePermission()) return false;

        NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /***************************
     * Private Utility Methods *
     ***************************/

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isWifiTypeConnection(NetworkCapabilities networkCapabilities) {
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * JavaDoc (NetworkCapabilities.class): Indicates that connectivity on this network was successfully validated.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean isConnectionValidated(NetworkCapabilities networkCapabilities) {
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

    private boolean checkNetworkStatePermission() {

        if (!PermissionUtils.isPermissionGranted(mContext, Manifest.permission.ACCESS_NETWORK_STATE)) {
            Log.d(TAG, "isConnected() | Failed to check connectivity, ACCESS_NETWORK_STATE permission not asked or granted.");
            return true;
        }
        return false;
    }

    /**
     * Gets network information on the currently active network.
     *
     * @return - NetworkInfo describing currently active Network or null if failed to get ConnectivityService or activeNetworkInfo.
     */
    @SuppressLint("MissingPermission")
    private NetworkInfo getActiveNetworkInfo(){
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
    }
}
