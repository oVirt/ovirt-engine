package org.ovirt.engine.core.common.action;

import java.io.Serializable;

public enum LoginResult implements Serializable {
    Autheticated,
    CantAuthenticate,
    NoPermission,
    PasswordExpired;

    public int getValue() {
        return this.ordinal();
    }

    public static LoginResult forValue(int value) {
        return values()[value];
    }
}
