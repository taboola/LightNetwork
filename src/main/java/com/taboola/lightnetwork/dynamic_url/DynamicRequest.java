package com.taboola.lightnetwork.dynamic_url;

import android.text.TextUtils;
import android.util.Log;

import com.taboola.lightnetwork.dynamic_url.annotations.Body;
import com.taboola.lightnetwork.dynamic_url.annotations.TrackHeader;
import com.taboola.lightnetwork.dynamic_url.annotations.GET;
import com.taboola.lightnetwork.dynamic_url.annotations.POST;
import com.taboola.lightnetwork.dynamic_url.annotations.Path;
import com.taboola.lightnetwork.dynamic_url.annotations.Query;
import com.taboola.lightnetwork.dynamic_url.annotations.QueryMap;
import com.taboola.lightnetwork.dynamic_url.annotations.REQUEST_TYPE;
import com.taboola.lightnetwork.protocols.http.HttpManager;
import com.taboola.lightnetwork.url_components.MutableUrl;
import com.taboola.lightnetwork.url_components.PathParam;
import com.taboola.lightnetwork.url_components.UrlParameter;

import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Don't use this class directly, use {@link NetworkExecutable#create(Class)}.
 */
public class DynamicRequest {
    private static final String TAG = DynamicRequest.class.getSimpleName();
    private HttpManager mHttpManager;
    private String mUrlString;
    private int mRequestType;
    private JSONObject mJsonBody;
    private String mTrackHeaderByGroup; //If not empty, headers will be re-sent to server in annotated requests (grouped by TrackHeader value).
    private String mTrackHeadersKey; //The headers key to process group tracking
    private String mBaseUrl;

    DynamicRequest(HttpManager httpManager, Method method, String baseUrl, Object[] args) {
        mHttpManager = httpManager;
        mBaseUrl = baseUrl;

        // Process DynamicUrl method
        buildRequest(method, args);
    }

    /**********
     * Public *
     **********/

    /**
     * Execute the network call associated with this DynamicRequest.
     * This will not return any callback.
     */
    public void execute() {
        execute(null);
    }

    /**
     * Execute the network call associated with this DynamicRequest.
     * This will return an asynchronous response callback on the thread the request was called from.
     */
    public void execute(HttpManager.NetworkResponse networkResponse) {
        switch (mRequestType) {
            case REQUEST_TYPE.GET:
                mHttpManager.get(mUrlString, mTrackHeadersKey, mTrackHeaderByGroup, networkResponse);
                break;

            case REQUEST_TYPE.POST:
                mHttpManager.post(mUrlString, mJsonBody, mTrackHeadersKey, mTrackHeaderByGroup, networkResponse);
                break;

            default:
                Log.e(TAG, "Error processing method, methodType unrecognized");
        }
    }

    /**
     * Currently used mostly for testing.
     * @return - The final url built
     */
    public String getFinalUrl() {
        return mUrlString;
    }

    /**
     * @return - A JSONObject representing the request body if set, null otherwise.
     */
    public JSONObject getJsonBody() {
        return mJsonBody;
    }

    /***********
     * Private *
     ***********/

    private void buildRequest(Method method, Object[] args) {
        mRequestType = getRequestType(method);
        String baseUrl = getBaseUrl(method);
        parseAnnotations(baseUrl, method, args);
        determineHeaderTracking(method);
    }

    private void determineHeaderTracking(Method method) {
        TrackHeader trackHeaderAnnotation = method.getAnnotation(TrackHeader.class);
        if (trackHeaderAnnotation != null) {
            mTrackHeaderByGroup = trackHeaderAnnotation.group();
            mTrackHeadersKey = trackHeaderAnnotation.headerReadKey();
        }
    }


    private void parseAnnotations(String baseUrl, Method method, Object[] args) {
        //Define a mutable Url
        MutableUrl mutableUrl = new MutableUrl(baseUrl);

        //Parse annotations by type
        //First [] is the different parameters. Second [] is the Annotations group for each parameter.
        Annotation[][] perParametersAnnotations = method.getParameterAnnotations();

        if (perParametersAnnotations != null) {
            for (int parameter = 0; parameter < perParametersAnnotations.length; parameter++) { //for every parameter
                for (int annotation = 0; annotation < perParametersAnnotations[parameter].length; annotation++) {

                    //tmpAnnotation iterates over all annotations that belong to a specific parameter
                    Annotation tmpAnnotation = perParametersAnnotations[parameter][annotation];

                    //Handle every parameter that has an annotation, according to its type
                    if (tmpAnnotation instanceof Path) {
                        handlePathAnnotation(args[parameter], mutableUrl, tmpAnnotation);
                    } else if (tmpAnnotation instanceof Query) {
                        handleQueryAnnotation(args[parameter], mutableUrl, tmpAnnotation);
                    } else if (tmpAnnotation instanceof QueryMap) {
                        handleQueryMapAnnotation(args[parameter], mutableUrl);
                    } else if (tmpAnnotation instanceof Body) {
                        handleBodyAnnotation(args[parameter]);
                    } else {
                        Log.e(TAG, "Annotation not recognized: " + tmpAnnotation.annotationType());
                    }

                }
            }
        }

        //Extract final url
        mUrlString = mutableUrl.getUrl();
        Log.d(TAG, "parseAnnotation | finalUrl = " + mUrlString);
    }

    private void handlePathAnnotation(Object arg, MutableUrl mutableUrl, Annotation tmpAnnotation) {
        String pathKey = ((Path)tmpAnnotation).value();
        Object pathVal = arg;

        mutableUrl.addPathParam(new PathParam(pathKey, String.valueOf(pathVal)));
    }

    private void handleQueryAnnotation(Object arg, MutableUrl mutableUrl, Annotation tmpAnnotation) {
        String queryKey = ((Query)tmpAnnotation).value();
        Object queryVal = arg;

        mutableUrl.addQueryParameter(new UrlParameter(queryKey, queryVal));
    }

    private void handleQueryMapAnnotation(Object arg, MutableUrl mutableUrl) {
        if (! (arg instanceof Map)) {
            throw new RuntimeException("DynamicRequest | parseAnnotation | @QueryMap parameter type must be Map.");
        }

        //noinspection unchecked
        Map<Object, Object> parameterMap = (Map<Object, Object>) arg;

        for (Map.Entry<Object, Object> mapParameter : parameterMap.entrySet()){
            String queryMapParamKey = String.valueOf(mapParameter.getKey());
            String queryMapParamValue = String.valueOf(mapParameter.getValue());

            mutableUrl.addQueryParameter(new UrlParameter(queryMapParamKey, queryMapParamValue));
        }
    }

    private void handleBodyAnnotation(Object arg) {
        if (! (arg instanceof JSONObject)) {
            throw new RuntimeException("DynamicRequest | parseAnnotation | @Body parameter type must be of type org.json.JSONObject.");
        }

        mJsonBody = (JSONObject) arg;
    }

    /**
     *
     * @param method - The interface method of which annotation is examined.
     * @return - Returned value corresponds to available set in {@link REQUEST_TYPE}
     */
    private int getRequestType(Method method) {
        if (method.getAnnotation(GET.class) != null) {
            return REQUEST_TYPE.GET;
        } else if (method.getAnnotation(POST.class) != null) {
            return REQUEST_TYPE.POST;
        }

        throw new RuntimeException("DynamicRequest | getRequestType | Request interface must declare a known Http method (See REQUEST_TYPE) in method annotation.");
    }

    private String getBaseUrl(Method method) {
        //Start with global base url or empty String if required.
        StringBuilder urlBuilder = new StringBuilder(TextUtils.isEmpty(mBaseUrl)? "" : mBaseUrl);

        switch (mRequestType){
            case REQUEST_TYPE.GET:
                urlBuilder.append(method.getAnnotation(GET.class).value());
                break;
            case REQUEST_TYPE.POST:
                urlBuilder.append(method.getAnnotation(POST.class).value());
                break;
            default:
                throw new RuntimeException("DynamicRequest | getBaseUrl | Request interface must declare a known Http method (See REQUEST_TYPE) in method annotation.");
        }

        return urlBuilder.toString();
    }



}
