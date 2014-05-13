package org.ovirt.engine.core.common.businessentities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableOnVdsStatus {
    VDSStatus[] statuses() default { VDSStatus.NonResponsive, VDSStatus.Maintenance, VDSStatus.Down,
            VDSStatus.Unassigned, VDSStatus.InstallFailed, VDSStatus.PendingApproval, VDSStatus.InstallingOS };

}
