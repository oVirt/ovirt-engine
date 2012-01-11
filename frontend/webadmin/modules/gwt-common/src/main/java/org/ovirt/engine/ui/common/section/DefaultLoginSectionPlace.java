package org.ovirt.engine.ui.common.section;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Binding annotation for String constant representing default place of "login" section.
 * <p>
 * Login section is basically a collection of places accessible to anonymous (unauthenticated) users.
 */
@BindingAnnotation
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultLoginSectionPlace {

}
