package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.Guid;

public enum PredefinedUsers {
    ADMIN_USER(new Guid("fdfc627c-d875-11e0-90f0-83df133b58cc"));

    private Guid id;

    private PredefinedUsers(Guid value) {
        id = value;
    }

    public Guid getId() {
        return id;
    }
}
