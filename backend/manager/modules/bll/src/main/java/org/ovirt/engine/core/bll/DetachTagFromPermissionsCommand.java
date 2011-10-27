package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;

public class DetachTagFromPermissionsCommand<T extends AttachEntityToTagParameters> extends
        AttachDetachTagToEntityCommandBase<T> {
    public DetachTagFromPermissionsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        /*
         * for (Guid permissionId : getEntitiesList()) { if (getTagId() != null
         * && DbFacade.getInstance().GetTagsByTagIdAndPermissionId(getTagId(),
         * permissionId) != null) { permissions permission =
         * DbFacade.getInstance().GetPermissionById(permissionId); // if (vm !=
         * null) // { // AppendCustomValue("VmsNames", vm.vm_name, ", "); // }
         * DbFacade.getInstance().RemoveTagFromPermission(new
         * tags_permissions_map(permissionId, getTagId())); setSucceeded(true);
         * } }
         */
    }
}
