package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "CreateStoragePoolVDSCommandParameters")
public class CreateStoragePoolVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "StorageType")
    private StorageType privateStorageType = StorageType.forValue(0);

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "StoragePoolName")
    private String privateStoragePoolName;

    public String getStoragePoolName() {
        return privateStoragePoolName;
    }

    private void setStoragePoolName(String value) {
        privateStoragePoolName = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "MasterDomainId")
    private Guid privateMasterDomainId = new Guid();

    public Guid getMasterDomainId() {
        return privateMasterDomainId;
    }

    private void setMasterDomainId(Guid value) {
        privateMasterDomainId = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "DomainsIdList")
    private java.util.ArrayList<Guid> privateDomainsIdList;

    public java.util.ArrayList<Guid> getDomainsIdList() {
        return privateDomainsIdList;
    }

    private void setDomainsIdList(java.util.ArrayList<Guid> value) {
        privateDomainsIdList = value;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "MasterVersion")
    private int privateMasterVersion;

    public int getMasterVersion() {
        return privateMasterVersion;
    }

    private void setMasterVersion(int value) {
        privateMasterVersion = value;
    }

    public CreateStoragePoolVDSCommandParameters(Guid vdsId, StorageType storageType, Guid storagePoolId,
            String poolName, Guid masterDomainId, java.util.ArrayList<Guid> domainsIdList, int masterVersion) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
        setStorageType(storageType);
        setStoragePoolName(poolName);
        setMasterDomainId(masterDomainId);
        setDomainsIdList(domainsIdList);
        setMasterVersion(masterVersion);
    }

    public CreateStoragePoolVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storagePoolId=%s, storageType=%s, storagePoolName=%s, masterDomainId=%s, " +
                "domainsIdList=%s, masterVersion=%s",
                super.toString(),
                getStoragePoolId(),
                getStorageType(),
                getStoragePoolName(),
                getMasterDomainId(),
                getDomainsIdList(),
                getMasterVersion());
    }

}
