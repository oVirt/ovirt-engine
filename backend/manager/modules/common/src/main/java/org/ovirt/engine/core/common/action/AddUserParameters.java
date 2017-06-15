package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;

public class AddUserParameters extends ActionParametersBase {

    /**
     *
     */
    private static final long serialVersionUID = 5238452182295928273L;
    private DbUser userToAdd;

    public AddUserParameters(DbUser userToAdd) {
        this.userToAdd = userToAdd;
    }

    public AddUserParameters() {
    }

    public void setUserToAdd(DbUser userToAdd) {
        this.userToAdd = userToAdd;
    }

    public DbUser getUserToAdd() {
        return userToAdd;
    }

}
