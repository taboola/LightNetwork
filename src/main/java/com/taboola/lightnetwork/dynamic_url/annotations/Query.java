package com.taboola.lightnetwork.dynamic_url.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation allows runtime setting of the value for associated url's key=val query pair.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Query {
    String value();
}
