package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.utils.VmDeviceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableDeviceOnVmStatusField {

    VMStatus[] statuses() default { VMStatus.Down };

    VmDeviceGeneralType generalType();

    VmDeviceType type();

    boolean isReadOnly() default false;
}
