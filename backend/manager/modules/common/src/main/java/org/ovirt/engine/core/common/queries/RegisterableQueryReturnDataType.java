package org.ovirt.engine.core.common.queries;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RegisterableQueryReturnDataType")
public enum RegisterableQueryReturnDataType implements Serializable {
    UNDEFINED,
    IQUERYABLE,
    LIST_IQUERYABLE,
    SEARCH;

    public int getValue() {
        return this.ordinal();
    }

    public static RegisterableQueryReturnDataType forValue(int value) {
        return values()[value];
    }
}
