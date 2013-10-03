package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.permissions;

public class PermissionsOperationsParametes extends VdcActionParametersBase {
    private static final long serialVersionUID = 8854712438369127152L;

    public PermissionsOperationsParametes(permissions permission) {
        setPermission(permission);
    }

    public PermissionsOperationsParametes(permissions permission, DbUser user) {
        this(permission);
        this.user = user;
    }

    public PermissionsOperationsParametes(permissions permission, LdapGroup adGroup) {
        this(permission);
        setAdGroup(adGroup);
    }

    private DbUser user;

    public DbUser getUser() {
        return user;
    }

    public void setUser(DbUser value) {
        user = value;
    }

    private LdapGroup privateAdGroup;

    public LdapGroup getAdGroup() {
        return privateAdGroup;
    }

    public void setAdGroup(LdapGroup value) {
        privateAdGroup = value;
    }

    private permissions privatePermission;

    public permissions getPermission() {
        return privatePermission;
    }

    public void setPermission(permissions value) {
        privatePermission = value;
    }

    public PermissionsOperationsParametes() {
    }
}
