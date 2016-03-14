package org.ovirt.engine.core.common.businessentities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates editable VDS fields. By default, fields with this
 * annotation are editable in any VDS status. The statuses during
 * which a field is editable can be restricted by adding those
 * statuses in which the VDS may be edited to onStatuses().
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableVdsField {
    VDSStatus[] onStatuses() default {};
}
