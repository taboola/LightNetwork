package com.taboola.lightnetwork.url_components;

import android.net.Uri;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * MutableUrl Structure:
 * BaseUrl (Mandatory): Includes the whole url excluding the query part. Optionally '?' character.
 * Query (Optional): A list of key values represented by {@link UrlParameters}.
 */
public class MutableUrl {
    private static final String STRUCTURE = "%s%s"; //BaseUrl (incl. optional '?' char.) + QueryParams
    private static final boolean UTF8_BY_DEFAULT = true;

    private String mBaseUrl;
    private UrlParameters mQueryParameters;
    private ArrayList<PathParam> mPathParams;

    /**
     * Build with a mandatory baseUrl.
     *
     * Valid Examples:
     * - http://www.exmaple.com
     * - https://www.server.com/{pathPart1}/file.json
     *
     * @param baseUrl - Includes: Protocol + :// + DomainName + "/" + Path. Excludes: '?' and query params.
     */
    public MutableUrl(String baseUrl) {
        if (TextUtils.isEmpty(baseUrl)){
            throw new RuntimeException("constructor | baseUrl cannot be null.");
        }

        // Treat with/without '/' suffix the same.
        if (baseUrl.endsWith("/")){
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        mBaseUrl = baseUrl;
    }


    /*******************
     * Path Parameters *
     *******************/

    public MutableUrl addPathParam(PathParam pathParam) {
        if (mPathParams == null){
            mPathParams = new ArrayList<>();
        }

        mPathParams.add(pathParam);
        return this;
    }

    private String getBaseUrlWithParams() {
        String baseUrl = mBaseUrl;

        if (mPathParams != null) {
            for (PathParam pathParam : mPathParams) {
                baseUrl = baseUrl.replace(pathParam.getOldVal(), UTF8_BY_DEFAULT ? Uri.encode(pathParam.getNewVal()) : pathParam.getNewVal());
            }
        }

        return baseUrl;
    }


    /********************
     * Query Parameters *
     ********************/

    public MutableUrl addQueryParameter(UrlParameter queryParameter){
        if (mQueryParameters == null){
            mQueryParameters = new UrlParameters();
        }

        mQueryParameters.addParameters(queryParameter);
        return this;
    }

    private String getQueryParams() {
        if (mQueryParameters == null || mQueryParameters.isEmpty()) {
            return "";
        }

        return "?" + mQueryParameters.getString(true);
    }


    /*********************
     * Getters / setters *
     *********************/

    public String getUrl() {
        return String.format(STRUCTURE, getBaseUrlWithParams(), getQueryParams());
    }

}