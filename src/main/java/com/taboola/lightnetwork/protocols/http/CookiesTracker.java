package com.taboola.lightnetwork.protocols.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.taboola.lightnetwork.utils.SharedPrefUtil;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class allows to track specific headers in Cookie-like fashion. It does not provide full cookies support.
 */
public class CookiesTracker {
    private static final String TAG = CookiesTracker.class.getSimpleName();
    private static final String HEADER_DELIMITER = ";"; //Traditional headers separator.
    private Context mApplicationContext;

    CookiesTracker(Context context) {
        mApplicationContext = context;
    }

    /**
     * Will load all headers previously saved under the specified headers group.
     * @param connection - The HttpUrlConnection that the headers should be loaded into.
     * @param trackHeadersByGroup - The headers grouping channel requested to load.
     */
    public synchronized void setCookiesInRequest(HttpURLConnection connection, String trackHeadersByGroup) {
        //Load connection headers list, by grouping (called trackHeader)
        if (trackHeadersByGroup != null) {
            Map<String, String> taboolaHeaderFields = SharedPrefUtil.getTrackHeadersMap(mApplicationContext, trackHeadersByGroup);

            if (taboolaHeaderFields != null) {
                Log.d(TAG, "Url = " + connection.getURL() + ", trackHeadersByGroup = " + trackHeadersByGroup + ", setHeadersInRequest: " + Arrays.toString(taboolaHeaderFields.entrySet().toArray()));
                addHeaderMapToRequest(connection, taboolaHeaderFields);
            }
        }
    }

    private void addHeaderMapToRequest(HttpURLConnection connection, Map<String, String> headers) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }
    }

    /**
     *  Current implementation handles a separate channel for each "trackHeadersByGroup" group (as can be set in the Annotation {@link com.taboola.lightnetwork.dynamic_url.annotations.TrackHeader})
     *  All, and only <trackHeadersKey> headers will be saved/loaded using SharedPreferences, on a per channel basis.
     */
    public synchronized void getCookiesFromResponse(HttpURLConnection connection, String trackHeadersKey, String trackHeadersByGroup) {
        if (trackHeadersKey != null && trackHeadersByGroup != null) { //default is "Global", empty not expected.
            Map<String, List<String>> headerFields = connection.getHeaderFields();

            //Get headers for defined trackHeadersByGroup from response
            Map<String, String> taboolaHeaderFields = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
                String key = entry.getKey();

                //Identify Taboola header lists.
                if (key != null && key.toLowerCase().contains(trackHeadersKey)) {
                    try {
                        //Add all taboola headers to a list.
                        taboolaHeaderFields.put(key, TextUtils.join(HEADER_DELIMITER, entry.getValue())); //re-adding headers to HttpUrlConnection later works with <String, String>, that's why we're joining here.
                        Log.d(TAG, "getHeadersFromResponse: " + Arrays.toString(taboolaHeaderFields.entrySet().toArray()));
                    } catch (Exception e) {
                        Log.e(TAG, "Problem parsing headers. Error: " + e.getLocalizedMessage(), e);
                    }
                }
            }

            Log.d(TAG, "Url = " + connection.getURL() + ", trackHeadersByGroup = " + trackHeadersByGroup + ", getHeadersFromResponse: " + Arrays.toString(taboolaHeaderFields.entrySet().toArray()));

            //Save a connection headers list, by grouping (called trackHeader)
            SharedPrefUtil.setTrackHeadersMap(mApplicationContext, taboolaHeaderFields, trackHeadersByGroup);
        }
    }

}