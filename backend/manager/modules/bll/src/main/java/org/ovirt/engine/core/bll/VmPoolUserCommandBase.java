package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public abstract class VmPoolUserCommandBase<T extends VmPoolUserParameters> extends VmPoolSimpleUserCommandBase<T> {
    protected VmPoolUserCommandBase(Guid commandId) {
        super(commandId);
    }

    public VmPoolUserCommandBase(T parameters) {
        super(parameters);
    }

    protected void initUser() {
        DbUser user = getDbUser();
        if (user != null && user.getId() == null) {
            user.setId(Guid.newGuid());
            getDbUserDAO().save(user);
        }
    }
}
