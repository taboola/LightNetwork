package com.taboola.lightnetwork.dynamic_url.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation allows adding a JSONObject body to a POST request.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Body {

}
