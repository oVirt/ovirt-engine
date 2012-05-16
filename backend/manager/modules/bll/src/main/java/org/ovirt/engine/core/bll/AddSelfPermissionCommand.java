package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;

public class AddSelfPermissionCommand<T extends PermissionsOperationsParametes> extends AddPermissionCommand<T> {
    private static final long serialVersionUID = 3521308383376252911L;

    public AddSelfPermissionCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = true;
        if (!(getParameters().getPermission().getrole_id().equals(new Guid("00000000-0000-0000-0001-000000000001")) || getParameters()
                .getParentCommand() == VdcActionType.AddSelfPermission)) {
            returnValue = false;
        }
        return returnValue;
    }
}
