package com.taboola.lightnetwork.dynamic_url.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation allows dynamic runtime replacing of path parameters in the url.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Path {
    String value();
}
