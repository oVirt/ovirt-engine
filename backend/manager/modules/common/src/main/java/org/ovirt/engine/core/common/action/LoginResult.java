package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "LoginResult")
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
