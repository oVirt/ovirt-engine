package org.ovirt.engine.core.common.interfaces;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SearchType", namespace = "http://service.engine.ovirt.org")
public enum SearchType implements Serializable {
    VM,
    VDS,
    VmTemplate,
    AuditLog,
    AdUser,
    AdGroup,
    DBUser,
    VmPools,
    Cluster,
    StoragePool,
    StorageDomain;

    public int getValue() {
        return this.ordinal();
    }

    public static SearchType forValue(int value) {
        return values()[value];
    }
}
