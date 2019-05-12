package com.taboola.lightnetwork.dynamic_url;

import com.taboola.lightnetwork.protocols.http.HttpManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Cheap imitation of Retrofit.
 *
 * Usage example:
 * --------------
 *
 *    // Define NetworkExecutable instance
 *    NetworkExecutable networkExecutable = new NetworkExecutable(<networkManager>);
 *
 *        // Create network object
 *        SampleNetworkApi sampleNetworkApi = networkExecutable.create(SampleNetworkApi.class);
 *            // Access network API synchronously
 *            sampleNetworkApi.sampleNetworkRequest1.execute();
 *
 *            // Access network API using a NetworkResponse callback
 *            sampleNetworkApi.sampleNetworkRequest2.execute(new NetworkResponse(){..});
 *    }
 *
 *    public interface SampleNetworkApi {
 *        DynamicRequest sampleNetworkRequest1();
 *        DynamicRequest sampleNetworkRequest2();
 *    }
 */
public class NetworkExecutable {
    private static final String TAG = NetworkExecutable.class.getSimpleName();
    private HttpManager mHttpManager;
    private String mBaseUrl;

    public NetworkExecutable(final HttpManager httpManager) {
        this(httpManager, null);
    }

    public NetworkExecutable(final HttpManager httpManager, String baseUrl) {
        mHttpManager = httpManager;
        mBaseUrl = baseUrl;
    }

    /**
     * A Factory method that "Proxy"s objects from given Interface classes.
     *
     * Two returns:
     * - The create method returns the network local API class with the different network calls.
     * - The invoke method returns the network request object itself, which allows executing the network call.
     */
    public <T> T create(final Class<T> interfaceClass) {
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //Allow Object methods to work as expected.
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }

                //Return executable networking interface object.
                return new DynamicRequest(mHttpManager, method, mBaseUrl, args);
            }
        });

    }

}