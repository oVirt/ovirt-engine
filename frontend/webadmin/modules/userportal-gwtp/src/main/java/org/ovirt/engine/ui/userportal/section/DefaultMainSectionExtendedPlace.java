package org.ovirt.engine.ui.userportal.section;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Similar to {@link org.ovirt.engine.ui.common.section.DefaultMainSectionPlace DefaultMainSectionPlace}, represents the
 * default place of "main" section for "Extended" main tab.
 */
@BindingAnnotation
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultMainSectionExtendedPlace {

}
