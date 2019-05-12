package com.taboola.lightnetwork.protocols.http;

import android.content.Context;
import android.net.http.HttpResponseCache;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import java.util.Map;

/**
 * This component is in charge of providing basic Http network functionality.
 */
public class HttpManager {
    private static final String TAG = HttpManager.class.getSimpleName();
    private static final int DEFAULT_TIMEOUT_MILLIS = 10000;
    private static final int MAX_HTTP_RESPONSE_CACHE_SIZE_MB = 10;
    private HeadersManager mHeadersManager;
    private CookiesTracker mCookiesTracker;

    public HttpManager(Context context) {
        mHeadersManager = new HeadersManager();
        mCookiesTracker = new CookiesTracker(context);
        setupCache(context);
    }

    /**********
     * PUBLIC *
     **********/

    /**
     * Same as {@link #get(String, HttpManager.NetworkResponse)} without expecting a callback.
     * @param url - The end point for the request.
     */
    public void get(final String url) {
        new HttpGet(mHeadersManager, mCookiesTracker, DEFAULT_TIMEOUT_MILLIS).get(url, null, null, null, null);
    }

    /**
     * Use this to get a Network response.
     * @param url - The full url for the get request. Url must begin with http:// or https:// prefix.
     * @param networkResponse - A callback interface returning either the server response or an error event.
     */
    public void get(String url, HttpManager.NetworkResponse networkResponse) {
        new HttpGet(mHeadersManager, mCookiesTracker, DEFAULT_TIMEOUT_MILLIS).get(url, null, null, null, networkResponse);
    }

    /**
     * Use this to get a Network response.
     * @param url - The full url for the get request. Url must begin with http:// or https:// prefix.
     * @param networkResponse - A callback interface returning either the server response or an error event.
     * @param trackHeadersByGroup - Headers handled according to this grouping.
     */
    public void get(String url, String trackHeadersKey, String trackHeadersByGroup, HttpManager.NetworkResponse networkResponse) {
        new HttpGet(mHeadersManager, mCookiesTracker, DEFAULT_TIMEOUT_MILLIS).get(url, null, trackHeadersKey, trackHeadersByGroup, networkResponse);
    }

    /**
     * Use this to get a Network response.
     * @param url - The full url for the get request. Url must begin with http:// or https:// prefix.
     * @param requestHeaders - Headers map. Will be attached to request.
     * @param networkResponse - A callback interface returning either the server response or an error event.
     * @param trackHeadersByGroup - Headers handled according to this grouping.
     */
    public void get(String url, Map<String, String> requestHeaders, String trackHeadersKey, String trackHeadersByGroup, HttpManager.NetworkResponse networkResponse) {
        new HttpGet(mHeadersManager, mCookiesTracker, DEFAULT_TIMEOUT_MILLIS).get(url, requestHeaders, trackHeadersKey, trackHeadersByGroup, networkResponse);
    }

    /**
     * Same as {@link #get(String, HttpManager.NetworkResponse)} without expecting a callback.
     * @param url - The end point for the request.
     * @param jsonBody - Json body (optional)
     */
    public void post(final String url, final JSONObject jsonBody) {
        new HttpPost(mHeadersManager, mCookiesTracker, DEFAULT_TIMEOUT_MILLIS).post(url, jsonBody, null, null, null, null);
    }

    /**
     * Use this to get a Network response.
     * @param url - The full url for the get request. Url must begin with http:// or https:// prefix.
     * @param networkResponse - A callback interface returning either the server response or an error event.
     * @param jsonBody - Json body (optional)
     */
    public void post(String url, final JSONObject jsonBody, HttpManager.NetworkResponse networkResponse) {
        new HttpPost(mHeadersManager, mCookiesTracker, DEFAULT_TIMEOUT_MILLIS).post(url, jsonBody, null, null, null, networkResponse);
    }

    /**
     * Same as {@link #post(String, JSONObject, NetworkResponse)} but allows
     * @param url - The full url for the get request. Url must begin with http:// or https:// prefix.
     * @param networkResponse - A callback interface returning either the server response or an error event.
     * @param jsonBody - Json body (optional)
     * @param trackHeadersByGroup - Headers handled according to this grouping.
     */
    public void post(String url, final JSONObject jsonBody, String trackHeadersKey, String trackHeadersByGroup, HttpManager.NetworkResponse networkResponse) {
        new HttpPost(mHeadersManager, mCookiesTracker, DEFAULT_TIMEOUT_MILLIS).post(url, jsonBody, null, trackHeadersKey, trackHeadersByGroup, networkResponse);
    }

    /**
     * Same as {@link #post(String, JSONObject, String, NetworkResponse)} but allows adding request headers.
     * @param url - The full url for the get request. Url must begin with http:// or https:// prefix.
     * @param jsonBody - Json body (optional)
     * @param requestHeaders - Headers map. Will be attached to request. Can be null.
     * @param networkResponse - A callback interface returning either the server response or an error event. Can be null.
     * @param trackHeadersByGroup - Headers handled according to this grouping. Can be null.
     */
    public void post(String url, final JSONObject jsonBody, Map<String, String> requestHeaders, String trackHeadersKey, String trackHeadersByGroup, HttpManager.NetworkResponse networkResponse) {
        new HttpPost(mHeadersManager, mCookiesTracker, DEFAULT_TIMEOUT_MILLIS).post(url, jsonBody, requestHeaders, trackHeadersKey, trackHeadersByGroup, networkResponse);
    }

    /***********
     * PRIVATE *
     ***********/

    /**
     * Allow automatic caching of HTTP requests.
     * Note: Adding cache after max size reached will override oldest cache.
     * Note: This method does not require additional permission since it uses internal app storage.
     *
     * Source: https://developer.android.com/reference/android/net/http/HttpResponseCache
     * @param context
     */
    private void setupCache(Context context) {
        try {
            File httpCacheDir = new File(context.getCacheDir(), "http");
            long httpCacheSize = MAX_HTTP_RESPONSE_CACHE_SIZE_MB * 1024 * 1024;
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (NullPointerException npe) {
            Log.e(TAG, "Cannot define cache size: " + npe.getLocalizedMessage());
        } catch (IOException e) {
            Log.e(TAG, "HTTP response cache installation failed:" + e);
        }
    }

    /**********
     * COMMON *
     **********/

    public interface NetworkResponse {
        void onResponse(HttpResponse response);
        void onError(HttpError error);
    }

}