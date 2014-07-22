package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;

public class VmPoolUserParameters extends VmPoolSimpleUserParameters implements Serializable {
    private static final long serialVersionUID = -5672324868972973061L;

    public VmPoolUserParameters(Guid vmPoolId, DbUser user, boolean isInternal) {
        super(vmPoolId, user.getId());
        setUser(user);
        setIsInternal(isInternal);
    }

    private DbUser user;

    public DbUser getUser() {
        return user;
    }

    private void setUser(DbUser value) {
        user = value;
    }

    private boolean privateIsInternal;

    public boolean getIsInternal() {
        return privateIsInternal;
    }

    private void setIsInternal(boolean value) {
        privateIsInternal = value;
    }

    public VmPoolUserParameters() {
    }
}
