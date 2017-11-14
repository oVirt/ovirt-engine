package org.ovirt.engine.core.common.businessentities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates editable VM fields. By default, fields with this
 * annotation are editable in any VM status. The statuses during
 * which a field is editable can be restricted by adding those
 * statuses in which the VM may be edited to onStatuses().
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableVmField {
    boolean onHostedEngine() default false;

    /**
     * Field is considered editable in these states provided
     * {@link org.ovirt.engine.core.common.action.VmManagementParametersBase#applyChangesLater}
     * is not set.
     */
    VMStatus.Group hotSettableOnStatus() default VMStatus.Group.NONE;

    VMStatus[] onStatuses() default {};
}
