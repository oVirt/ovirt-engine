package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

public class UpdateUserParameters extends ActionParametersBase {
    private static final long serialVersionUID = 5238452182295928273L;
    private DbUser userToUpdate;
    private boolean mergeOptions;

    public UpdateUserParameters(DbUser userToUpdate, boolean mergeOptions) {
        this.userToUpdate = userToUpdate;
        this.mergeOptions = mergeOptions;
    }

    public UpdateUserParameters() {
        userToUpdate = new DbUser();
    }

    public void setUserToUpdate(DbUser userToUpdate) {
        this.userToUpdate = userToUpdate;
    }

    public DbUser getUserToUpdate() {
        return userToUpdate;
    }

    public boolean isMergeOptions() {
        return mergeOptions;
    }
}
