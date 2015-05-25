package org.ovirt.engine.ui.common.system;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Binding annotation for String constant representing an application-specific prefix
 * applied to all {@link ClientStorage} key names.
 * <p>
 * Such prefix exists to avoid clashes between WebAdmin vs. UserPortal instances running
 * on the same browser (but not necessarily at the same time) where one instance might
 * read data originally persisted by another instance.
 */
@BindingAnnotation
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientStorageKeyPrefix {

}
