package org.ovirt.engine.ui.common.auth;

import org.ovirt.engine.ui.uicommonweb.auth.CurrentUserRole;

public class CommonCurrentUserRole implements CurrentUserRole {

    private boolean createInstanceOnly = false;

    public boolean isCreateInstanceOnly() {
        return createInstanceOnly;
    }

    public void setCreateInstanceOnly(boolean createInstanceOnly) {
        this.createInstanceOnly = createInstanceOnly;
    }
}
