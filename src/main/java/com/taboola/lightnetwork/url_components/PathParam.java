package com.taboola.lightnetwork.url_components;

import android.text.TextUtils;
import android.util.Log;

public class PathParam {
    private static final String TAG = PathParam.class.getSimpleName();

    private String mOldVal;
    private String mNewVal;

    public PathParam(String oldVal, String newVal) {
        setOldVal(oldVal);
        mNewVal = newVal;
    }

    /**
     * Make sure replaced String includes '{', '}' characters.
     */
    private void setOldVal(String oldVal) {
        if (TextUtils.isEmpty(oldVal)){
            Log.e(TAG, "DynamicUrl | PathParam | oldVal is null or empty.");
            mOldVal = "";
            return;
        }

        if (oldVal.startsWith("{") && oldVal.endsWith("}")){
            mOldVal = oldVal;
        } else {
            mOldVal = "{" + oldVal + "}";
        }
    }

    /**
     * @return - returns old value as user writes it for String utility to find and replace.
     */
    public String getOldVal() {
        return mOldVal;
    }

    public String getNewVal() {
        return mNewVal;
    }

}
