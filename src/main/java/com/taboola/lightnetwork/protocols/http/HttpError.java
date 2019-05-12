package com.taboola.lightnetwork.protocols.http;

public class HttpError {
    private static final int NO_CODE = -1;

    public int mCode;
    public String mMessage;

    public HttpError(String message) {
        this(NO_CODE, message);
    }

    public HttpError(int code, String message) {
        mCode = code;
        mMessage = message;
    }

    @Override
    public String toString() {
        return String.format("Http error! Code (%s), HttpResponse message: %s.", mCode == NO_CODE ? "NA" : mCode, mMessage == null ? "" : mMessage);
    }
}
