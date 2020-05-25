package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

public class UpdateUserParameters extends ActionParametersBase {
    private static final long serialVersionUID = 5238452182295928273L;
    private DbUser userToUpdate;

    public UpdateUserParameters(DbUser userToUpdate) {
        this.userToUpdate = userToUpdate;
    }

    public UpdateUserParameters() {
    }

    public void setUserToUpdate(DbUser userToUpdate) {
        this.userToUpdate = userToUpdate;
    }

    public DbUser getUserToUpdate() {
        return userToUpdate;
    }

}
