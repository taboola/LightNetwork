# Taboola Android SDK
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)

## Table Of Contents
1. [Getting Started](#1-getting-started)
2. [Proguard](#2-proguard)
3. [License](#3-license)


## 1. Getting Started

### 1.1. Minimum requirements

* Android version 4.0  (```android:minSdkVersion="14"```)

### 1.2. Incorporating the SDK

1. Download and add the source code to your project. Notethat this library does not require additional dependencies.


2. Include this line in your app’s AndroidManifest.xml to allow Internet access
 ```xml
 <uses-permission android:name="android.permission.INTERNET" />
 ```

3. In order to use optional network state checks, this library uses the NetworkInfo api. This requires the following permission
 ```xml
 <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />    
 ```

## 2. ProGuard
You can find proguard rules for Taboola LightNetwork in [proguard-rules.pro](/proguard-rules.pro) file.

At this moment just add the following line:
```xml
-keep class com.taboola.lightnetwork.** { *; }
```

## 3. License
This program is licensed under the Apache 2.0 License Agreement (the “License Agreement”).  By copying, using or redistributing this program, you agree with the terms of the License Agreement.  The full text of the license agreement can be found at https://github.com/taboola/LightNetwork/blob/master/LICENSE.
Copyright 2019 Taboola, Inc. All rights reserved.

## 4. Quick Use Guide

1. Define a network interface
```java
public interface SampleNetworkApi {      
  @GET("http://www.example.com/")
  public DynamicRequest sampleNetworkRequest1();

  
  @POST("https://www.example.com/")
  public DynamicRequest sampleNetworkRequest2();
}
```

2. Create a DynamicRequest object out of that interface
```java
// Define NetworkExecutable instance
NetworkExecutable networkExecutable = new NetworkExecutable(<networkManager>);

// Create network object
SampleNetworkApi sampleNetworkApi = networkExecutable.create(SampleNetworkApi.class);

// Access network API synchronously
sampleNetworkApi.sampleNetworkRequest1.execute();

// Access network API using a NetworkResponse callback
sampleNetworkApi.sampleNetworkRequest2.execute(new NetworkResponse(){..});

public interface SampleNetworkApi {
 DynamicRequest sampleNetworkRequest1();
 DynamicRequest sampleNetworkRequest2();
}
```

3. Request definition tags

3.1. Using Available Protocols:

Currently all requests can either be Http Get or Http Post. To define the protocol, put the relevant annotation on top of defined method in your interface.

```java
@GET - Perform HTTP GET request.
@GET("http://www.example.com/{mee}/{yaoo}")
```

```java
@POST - Perform POST request.
@POST("https://postman-echo.com/post")
```

3.2. Using Available Annotations:

```java
@Path - Replace path parameter in url.
@GET("http://www.example.com/{mee}/{yaoo}")
DynamicRequest getA(@Path("mee") String mee, @Path("yaoo") String yaoo);
```

```java
@Query - Add key=value query pair in url.
@GET("https://www.example.com/")
DynamicRequest getB(@Query("hai") String ya);
```

```java
@Body - Add JSONObject body to a POST request.
@POST("https://postman-echo.com/post")
DynamicRequest postA(@Body JSONObject jsonBody);
```
