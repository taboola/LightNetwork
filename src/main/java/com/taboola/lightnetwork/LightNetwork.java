package com.taboola.lightnetwork;

import android.content.Context;

import com.taboola.lightnetwork.protocols.http.HttpManager;


/**
 *  LightNetwork is meant to be as small as possible, supplying easy and fast access to networking using HttpUrlConnection.
 *  Aimed at providing network capabilities mainly, but not exclusively, to SDK libraries.
 */
public class LightNetwork {
    private HttpManager mHttpManager;
    private State mState;

    public LightNetwork(Context context) {
        mHttpManager = new HttpManager(context);
        mState = new State(context);
    }

    /*************
     * Protocols *
     *************/

    /**
     * Supply fast, easy use of Http protocol calls.
     * @return - A manager that supplies network calls over Http.
     */
    public HttpManager getHttpManager(){
       return mHttpManager;
    }

    /************************
     * Network Connectivity *
     ************************/

    /**
     * @return - State object for querying current network status.
     */
    public State getState() {
        return mState;
    }
}