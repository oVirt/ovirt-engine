package org.ovirt.engine.core.common.businessentities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * When a field is annotated with this annotation, it is possible to export its value using OVF in such a way that the
 * field value will not be taken into consideration when import takes place. It is also possible to specify using
 * logOption whether to report the export value to the audit log during import
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OvfExportOnlyField {

    public enum ExportOption {
        DONT_EXPORT, // Dont log fields annotated with the annotation, just
                     // write to OVF
        ALWAYS_EXPORT, // ALways log fields annotated with the annotation
        EXPORT_NON_IGNORED_VALUES; // Log fields annotated with the annotation,
                                   // only if their value differs from the
                                   // value stated at "valueToIgnore"
    };

    /**
     * Name of field as will be written in OVF
     */
    String name() default "";

    /**
     * String representation of value to ignore for logging
     */
    String valueToIgnore() default "";

    ExportOption exportOption() default ExportOption.EXPORT_NON_IGNORED_VALUES;

}
