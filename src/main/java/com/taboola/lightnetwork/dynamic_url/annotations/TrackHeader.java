package com.taboola.lightnetwork.dynamic_url.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation tells LightNetwork to save all headers from response and re-send them in consequent requests.
 * group - Tells LightNetwork to isolate headers to specific network calls only.
 *           Setting annotation without setting a group value adds annotated network call to a global grouping that reads and sends all non-grouped headers.
 * headerReadKey -  The response header key to be tracked.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface TrackHeader {
    String group() default "Global";
    String headerReadKey() default "x-TrackHeader";
}
