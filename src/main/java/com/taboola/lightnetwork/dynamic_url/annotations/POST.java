package com.taboola.lightnetwork.dynamic_url.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * This annotation instructs the request to perform an Http POST method.
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface POST {
    String value() default "";
}
