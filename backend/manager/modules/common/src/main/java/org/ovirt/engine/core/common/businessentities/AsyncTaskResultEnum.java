package org.ovirt.engine.core.common.businessentities;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AsyncTaskResultEnum")
public enum AsyncTaskResultEnum {
    success,
    failure,
    cleanSuccess,
    cleanFailure;

    public int getValue() {
        return this.ordinal();
    }

    public static AsyncTaskResultEnum forValue(int value) {
        return values()[value];
    }
}
