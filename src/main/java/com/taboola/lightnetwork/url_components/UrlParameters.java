package com.taboola.lightnetwork.url_components;

import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class is intended as a helper class to allow fast use of url key value chaining.
 * Notice: You can easily produce a key=value structure using the toString() method.
 */
public class UrlParameters {
    private static final String TAG = UrlParameters.class.getSimpleName();
    private static final String KEY_VALUE_PAIR = "%s=%s";

    protected Set<UrlParameter> mParameters; //No duplicate elements, order doesn't matter.

    public UrlParameters(){
        mParameters = new HashSet<>();
    }

    /***********
     * Data In *
     ***********/

    /**
     * Add any number of parameters to parameter Set.
     */
    public UrlParameters addParameters(UrlParameter... parameters) {
        for (UrlParameter urlParameter : parameters) {
            validateAndAddParameter(urlParameter);
        }

        return this;
    }

    /**
     * Legacy method, should service existing parameter HashMaps
     * @deprecated as of version 2.1.0. Use {@link #addParameters(UrlParameter... parameters)} instead.
     */
    @Deprecated
    public UrlParameters addParameters(HashMap<String, String> parameters) {
        Iterator iterator = parameters.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            validateAndAddParameter(new UrlParameter(pair.getKey(), pair.getValue()));
            iterator.remove();
        }

        return this;
    }

    /**
     * Make sure no bad structure parameters are added.
     */
    private void validateAndAddParameter(UrlParameter urlParameter){
        if (urlParameter.isValid()) {
            mParameters.add(urlParameter);
        } else {
            Log.e(TAG, "UrlParameters | addParameters() | Tried to add invalid parameter.");
        }
    }

    /************
     * Data Out *
     ************/

    /**
     * @return - By default returns a UTF-8 compatible key=val string.
     */
    @Override
    public String toString() {
        return getString(true);
    }

    /**
     * This toString() exports internal parameter data in the url key=value pattern, see KEY_VALUE_PAIR field.
     * @param utf8Encoded - should keys and values be encoded
     */
    public String getString(boolean utf8Encoded) {
        StringBuilder urlStringBuilder = new StringBuilder();

        boolean first = true;
        for(UrlParameter parameter : mParameters){
            if (first) {
                first = false;
            } else {
                urlStringBuilder.append("&");
            }

            String key = utf8Encoded ? Uri.encode(parameter.mKey) : parameter.mKey;
            String value = utf8Encoded ? Uri.encode(parameter.mValue) : parameter.mValue;
            urlStringBuilder.append(String.format(KEY_VALUE_PAIR, key, value));
        }

        return urlStringBuilder.toString(); //StringBuilder never returns a null String
    }

    public boolean isEmpty() {
        return mParameters == null || mParameters.isEmpty();
    }
}


