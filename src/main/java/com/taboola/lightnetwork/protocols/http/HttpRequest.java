package com.taboola.lightnetwork.protocols.http;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

abstract class HttpRequest {
    private static final String TAG = HttpRequest.class.getSimpleName();
    private static final String HEADER_FIELD_LOCATION = "Location"; //When server expects redirect, it mentions new url in a tag called Location.

    private int mConfiguredTimeout;
    private HeadersManager mHeadersManager;
    private CookiesTracker mCookiesTracker;
    private WeakReference<Looper> mOriginalLooper; //Looper from thread that created the

    Map<String, String> mRequestHeaders;
    String mUrl;

    //CookieTracking
    String mTrackHeadersByGroup; //If not empty, headers will be re-sent to server in annotated requests (grouped by mTrackHeadersByGroup value).
    String mTrackHeadersKey; //The headers key to process group tracking

    public HttpRequest(int configuredTimeout, HeadersManager headersManager, CookiesTracker cookiesTracker) {
        mConfiguredTimeout = configuredTimeout;
        mHeadersManager = headersManager;
        mCookiesTracker = cookiesTracker;
    }

    void performRequsetOnBackgroundThread(final HttpManager.NetworkResponse networkResponse) {
        //Remember calling thread's Looper.
        mOriginalLooper = new WeakReference<>(Looper.myLooper());

        //Run network on background thread.
        if (Looper.myLooper() == Looper.getMainLooper()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    performRequest(networkResponse);
                }
            }).start();
        } else {
            performRequest(networkResponse);
        }
    }

    private void performRequest(HttpManager.NetworkResponse networkResponse) {
        HttpURLConnection connection = null;
        try {
            // Cast http/s appropriately
            if (mUrl.toLowerCase().contains("https://")) {
                connection = (HttpsURLConnection) new URL(mUrl).openConnection();
            } else if (mUrl.toLowerCase().contains("http://")) {
                connection = (HttpURLConnection) new URL(mUrl).openConnection();
            } else {
                returnError(networkResponse, new HttpError("Url must begin with http:// or https://"));
                return;
            }

            // Apply headers, if relevant, to request
            mHeadersManager.setHeadersInRequest(connection, mRequestHeaders);

            // Allow CookiesTracker to update request
            mCookiesTracker.setCookiesInRequest(connection, mTrackHeadersByGroup);

            // Apply timeout for requests
            connection.setConnectTimeout(mConfiguredTimeout);
            connection.setReadTimeout(mConfiguredTimeout);

            // Legacy correction for User-Agent
            adjustUserAgent(connection);

            // Allow setup differences between different request types
            protocolSpecificConnectionSetup(connection);

            connection.connect();

            //Handle different response codes, ideally returning response to calling code
            handleResponse(networkResponse, connection);

        } catch (NullPointerException e) {
            Log.e(TAG, "performRequest error: " + e.getLocalizedMessage());
            returnError(networkResponse, new HttpError("NullPointerException: " + e.getLocalizedMessage()));
        } catch (MalformedURLException e) {
            Log.e(TAG, "performRequest error: " + e.getLocalizedMessage());
            returnError(networkResponse, new HttpError("MalformedURLException: " + e.getLocalizedMessage()));
        } catch (IOException e) {
            Log.e(TAG, "performRequest error: " + e.getLocalizedMessage());
            returnError(networkResponse, new HttpError("IOException: " + e.getLocalizedMessage()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * This code includes a standardization method that aligns the user-agent,
     * removing all non-ASCII characters.
     */
    private void adjustUserAgent(HttpURLConnection connection) {
        final String HTTP_AGENT = "http.agent"; //The key for System property of Http agent. Legacy requirement for adding to network calls.
        final String USER_AGENT_PROPERTY = "User-Agent"; //The key for HttpURLConnection user agent.

        String userAgent = System.getProperty(HTTP_AGENT);
        if (!TextUtils.isEmpty(userAgent)) {
            userAgent = removeNonAscii(userAgent);
            connection.setRequestProperty(USER_AGENT_PROPERTY, userAgent);
        }
    }

    private String removeNonAscii(String src) {
        return Normalizer.normalize(src, Normalizer.Form.NFD).replaceAll("[^\\x00-\\x7F]", "");
    }

    /**
     * Each different protocol sets up connection a little differently.
     * @throws IOException - Json handling and HttpUrlConection.setRequestMethod() can either produce this.
     */
    abstract void protocolSpecificConnectionSetup(HttpURLConnection httpUrlConnection) throws IOException;

    /**
     * Read response code and handle accordingly.
     * All 2xx codes are considered a success (Source: https://www.restapitutorial.com/httpstatuscodes.html)
     * 301, 302, 303 codes are considered redirect requests and will be performed accordingly by this client.
     * @throws IOException - InputStream exception, possible when reading connection data.
     */
    private void handleResponse(HttpManager.NetworkResponse networkResponse, HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();

        if (status >= 200 && status < 300){
            handleResponseOK(networkResponse, status, connection);
        } else if (status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_SEE_OTHER) { //response REDIRECT (303 see other is also recommended by few articles)
            handleResponseRedirect(networkResponse, connection);
        } else { //any other response code returns an error
            Log.v(TAG, "HttpRequest | handleResponse | error, response code = " + status);
            returnError(networkResponse, new HttpError("Invalid response code: " + status));
        }
    }

    private void handleResponseOK(HttpManager.NetworkResponse networkResponse, int statusCode, HttpURLConnection connection) throws IOException {
        //Read response headers
        Map<String, List<String>> headerFields = mHeadersManager.getHeadersFromResponse(connection);

        //Feed CookiesTracker
        mCookiesTracker.getCookiesFromResponse(connection, mTrackHeadersKey, mTrackHeadersByGroup);

        //Read response
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line).append("\n");
        }
        br.close();

        //return response
        returnResponse(networkResponse, new HttpResponse(statusCode, response.toString(), headerFields));
    }

    private void handleResponseRedirect(HttpManager.NetworkResponse networkResponse, HttpURLConnection connection) {
        mUrl = connection.getHeaderField(HEADER_FIELD_LOCATION);
        Log.v(TAG, "HttpRequest | handleResponse | redirect, url = " + mUrl);
        performRequest(networkResponse);
    }


    /**
     * If networkResponse is not null, returns successful response callback on UI thread.
     * Note: If looper/calling thread is no longer available, returning to main thread.
     *
     * @param networkResponse - A callback interface registered by user.
     * @param response - Object description of response.
     */
    private void returnResponse(final HttpManager.NetworkResponse networkResponse, final HttpResponse response) {
        if (networkResponse != null) {
            Looper looper = getBestThreadLooepr();

            new Handler(looper).post(new Runnable() {
                @Override
                public void run() {
                    networkResponse.onResponse(response);
                }
            });
        }
    }

    /**
     * If networkResponse is not null, returns error callback on UI thread.
     * Note: If looper/calling thread is no longer available, returning to main thread.
     * @param networkResponse - A callback interface registered by user.
     * @param error - Object description of error.
     */
    private void returnError(final HttpManager.NetworkResponse networkResponse, final HttpError error) {
        if (networkResponse != null) {
            Looper looper = getBestThreadLooepr();

            new Handler(looper).post(new Runnable() {
                @Override
                public void run() {
                    networkResponse.onError(error);
                }
            });
        }
    }

    private Looper getBestThreadLooepr() {
        Looper looper = mOriginalLooper.get();
        if (looper == null) {
            looper = Looper.getMainLooper();
            Log.e(TAG, "Network cannot return response callback on calling Thread. Is calling Thread still alive? Returning callback on main Thread.");
        }
        return looper;
    }
}
