package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddSANStorageDomainParameters")
public class AddSANStorageDomainParameters extends StorageDomainManagementParameter {
    private static final long serialVersionUID = 6386931158747982426L;
    @XmlElement(name = "LunIdsJport")
    private java.util.ArrayList<String> privateLunIds;

    public java.util.ArrayList<String> getLunIds() {
        return privateLunIds == null ? new ArrayList<String>() : privateLunIds;
    }

    public void setLunIds(java.util.ArrayList<String> value) {
        privateLunIds = value;
    }

    public AddSANStorageDomainParameters(storage_domain_static storageDomain) {
        super(storageDomain);
    }

    public AddSANStorageDomainParameters() {
    }
}
