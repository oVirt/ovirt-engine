package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;

//VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ExtendSANStorageDomainParameters")
public class ExtendSANStorageDomainParameters extends StorageDomainParametersBase {
    private static final long serialVersionUID = 1051216804598069936L;
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "LunIds")
    private java.util.ArrayList<String> privateLunIds;

    public java.util.ArrayList<String> getLunIds() {
        return privateLunIds == null ? new ArrayList<String>() : privateLunIds;
    }

    public void setLunIds(java.util.ArrayList<String> value) {
        privateLunIds = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    private java.util.ArrayList<LUNs> privateLunsList;

    public java.util.ArrayList<LUNs> getLunsList() {
        return privateLunsList == null ? new ArrayList<LUNs>() : privateLunsList;
    }

    public void setLunsList(java.util.ArrayList<LUNs> value) {
        privateLunsList = value;
    }

    public ExtendSANStorageDomainParameters(Guid storageDomainId, java.util.ArrayList<String> lunIds) {
        super(storageDomainId);
        setLunIds(lunIds);
    }

    public ExtendSANStorageDomainParameters() {
    }
}
