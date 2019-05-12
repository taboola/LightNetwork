package com.taboola.lightnetwork.protocols.http;

import java.util.List;
import java.util.Map;

public class HttpResponse {
    public int mCode;
    public String mMessage;
    public Map<String, List<String>> mHeaderFields;

    public HttpResponse(int code, String message, Map<String, List<String>> headerFields) {
        mCode = code;
        mMessage = message;
        mHeaderFields = headerFields;
    }

    @Override
    public String toString() {
        return String.format("Http response code (%s), HttpResponse message: %s.", mCode, mMessage == null ? "" : mMessage);
    }
}
