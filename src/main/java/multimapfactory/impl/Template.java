package multimapfactory.impl;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * states that the following object is a template and is not meant to be used
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Template {}
