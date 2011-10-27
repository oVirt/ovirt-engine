package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.List;

//VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ReconstructMasterVDSCommandParameters")
public class ReconstructMasterVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "StoragePoolName")
    private String privateStoragePoolName;

    public String getStoragePoolName() {
        return privateStoragePoolName;
    }

    private void setStoragePoolName(String value) {
        privateStoragePoolName = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "MasterDomainId")
    private Guid privateMasterDomainId = new Guid();

    public Guid getMasterDomainId() {
        return privateMasterDomainId;
    }

    private void setMasterDomainId(Guid value) {
        privateMasterDomainId = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "DomainsList")
    private List<storage_pool_iso_map> privateDomainsList;

    public List<storage_pool_iso_map> getDomainsList() {
        return privateDomainsList;
    }

    private void setDomainsList(List<storage_pool_iso_map> value) {
        privateDomainsList = value;
    }

    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement(name = "MasterVersion")
    private int privateMasterVersion;

    public int getMasterVersion() {
        return privateMasterVersion;
    }

    private void setMasterVersion(int value) {
        privateMasterVersion = value;
    }

    public ReconstructMasterVDSCommandParameters(Guid vdsId, Guid storagePoolId, String storagePoolName,
            Guid masterDomainId, List<storage_pool_iso_map> domainsList, int masterVersion) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
        setStoragePoolName(storagePoolName);
        setMasterDomainId(masterDomainId);
        setDomainsList(domainsList);
        setMasterVersion(masterVersion);
    }

    public ReconstructMasterVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storagePoolId = %s, storagePoolName = %s, masterDomainId = %s, masterVersion = %s, domainsList = [%s]",
                super.toString(),
                getStoragePoolId(),
                getStoragePoolName(),
                getMasterDomainId(),
                getMasterVersion(),
                getPrintableDomainsList());
    }

    private String getPrintableDomainsList() {
        StringBuilder sb = new StringBuilder();
        for (storage_pool_iso_map map : getDomainsList()) {
            sb.append("{ domainId: ");
            sb.append(map.getstorage_id());
            sb.append(", status: ");
            sb.append(map.getstatus().name());
            sb.append(" };");
        }
        return sb.toString();
    }
}
