package com.taboola.lightnetwork.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;

public class PermissionUtils {

    /**
     * Same as ContextCompat.checkSelfPermission(..), does not require support library.
     * @return - true if permission is granted for application process, false otherwise.
     */
    public static boolean isPermissionGranted(Context context, String permission) {
        return context.checkPermission(permission, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED;
    }

}
