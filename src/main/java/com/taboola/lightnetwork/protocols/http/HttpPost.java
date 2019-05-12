package com.taboola.lightnetwork.protocols.http;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;


class HttpPost extends HttpRequest {
    private JSONObject mJsonBody;

    public HttpPost(HeadersManager headersManager, CookiesTracker cookiesTracker, int configuredTimeout) {
        super(configuredTimeout, headersManager, cookiesTracker);
    }

    /**
     * Wrapper method for networking, protection for NetworkOnMainUI thread.
     * @param url - The end point for the request.
     * @param jsonBody - Json body (optional)
     * @param trackHeadersByGroup - Headers handled according to this grouping.
     * @param networkResponse - A callback listener for the response.
     */
    void post(final String url, final JSONObject jsonBody, Map<String, String> requestHeaders, String trackHeadersKey, String trackHeadersByGroup, final HttpManager.NetworkResponse networkResponse) {
        mUrl = url;
        mJsonBody = jsonBody;
        mRequestHeaders = requestHeaders;
        mTrackHeadersByGroup = trackHeadersByGroup;
        mTrackHeadersKey = trackHeadersKey;
        performRequsetOnBackgroundThread(networkResponse);
    }

    void protocolSpecificConnectionSetup(HttpURLConnection httpUrlConnection) throws IOException {
        httpUrlConnection.setRequestMethod("POST");
        httpUrlConnection.setDoInput(true);
        httpUrlConnection.setDoOutput(true);

        //Add json body to request
        addJsonBody(httpUrlConnection);
    }

    private void addJsonBody(HttpURLConnection httpUrlConnection) throws IOException {
        if (mJsonBody != null && mJsonBody.length() > 0) {
            DataOutputStream writer = new DataOutputStream(httpUrlConnection.getOutputStream());
            writer.writeBytes(mJsonBody.toString());
            writer.flush();
            writer.close();
        }
    }
}
