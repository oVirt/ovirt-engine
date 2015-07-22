package org.ovirt.engine.core.common.businessentities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.ovirt.engine.core.common.utils.VmDeviceType;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableDeviceOnVmStatusField {

    VMStatus[] statuses() default { VMStatus.Down };

    VmDeviceGeneralType generalType();

    VmDeviceType type();

    boolean isReadOnly() default false;

    String name() default "";

}
