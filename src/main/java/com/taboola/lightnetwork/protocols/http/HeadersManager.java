package com.taboola.lightnetwork.protocols.http;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * This class is meant to save and load connection headers for network calls that require header handling.
 */
class HeadersManager {

    public synchronized void setHeadersInRequest(HttpURLConnection connection, Map<String, String> requestHeaders) {
        if (requestHeaders != null) {
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
    }

    public synchronized Map<String, List<String>> getHeadersFromResponse(HttpURLConnection connection) {
        return connection.getHeaderFields();
    }

}