package com.taboola.lightnetwork.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SharedPrefUtil {
    private static final String TAG = SharedPrefUtil.class.getSimpleName();

    private static final String SHARED_PREFS_KEY = "com.taboola.lightnetwork.SHARED_PREFERENCES_KEY";
    private static final String TRACK_HEADER_SHARED_PREFS_KEY_PREFIX = "com.taboola.lightnetwork.TRACK_HEADER_SHARED_PREFS_KEY_%s";

    @SuppressLint("ApplySharedPref")
    public static void setTrackHeadersMap(Context context, Map<String, String> trackHeaderMap, String trackHeader) {
        if (context == null) {
            Log.e(TAG, "setTrackHeadersMap: cannot save headers with null context.");
            return;
        }

        //Deflate map
        JSONObject jsonObject = new JSONObject(trackHeaderMap);
        String jsonString = jsonObject.toString();

        //Save sPref
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(String.format(TRACK_HEADER_SHARED_PREFS_KEY_PREFIX, trackHeader), jsonString);
        editor.commit();
    }

    public static HashMap<String, String> getTrackHeadersMap(Context context, String trackHeader) {
        if (context == null) {
            Log.e(TAG, "setTrackHeadersMap: cannot set headers in request with null context.");
            return null;
        }

        //Load sPref
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        String jsonString = preferences.getString(String.format(TRACK_HEADER_SHARED_PREFS_KEY_PREFIX, trackHeader), null);

        //Inflate map
        HashMap<String, String> trackHeaderMap = new HashMap<>();

        if (!TextUtils.isEmpty(jsonString)){
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    String value = jsonObject.getString(key);
                    trackHeaderMap.put(key, value);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed inflating TrackHeaders map.", e);
            }
        }

        return trackHeaderMap;
    }

}
