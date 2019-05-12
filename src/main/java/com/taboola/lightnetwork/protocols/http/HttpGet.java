package com.taboola.lightnetwork.protocols.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

class HttpGet extends HttpRequest {

    public HttpGet(HeadersManager headersManager, CookiesTracker cookiesTracker, int configuredTimeout) {
        super(configuredTimeout, headersManager, cookiesTracker);
    }

    /**
     * Wrapper method for networking, protection for NetworkOnMainUI thread.
     * @param url - The end point for the request.
     * @param trackHeadersByGroup - Headers handled according to this grouping.
     * @param networkResponse - A callback listener for the response.
     */
    void get(final String url, Map<String, String> requestHeaders, String trackHeadersKey, String trackHeadersByGroup, final HttpManager.NetworkResponse networkResponse) {
        mUrl = url;
        mRequestHeaders = requestHeaders;
        mTrackHeadersByGroup = trackHeadersByGroup;
        mTrackHeadersKey = trackHeadersKey;
        performRequsetOnBackgroundThread(networkResponse);
    }

    void protocolSpecificConnectionSetup(HttpURLConnection httpUrlConnection) throws IOException {
        httpUrlConnection.setRequestMethod("GET");
        httpUrlConnection.setRequestProperty("Accept", "application/json");
    }
}