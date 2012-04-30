package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class CreateStoragePoolVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    private StorageType privateStorageType = StorageType.forValue(0);

    public StorageType getStorageType() {
        return privateStorageType;
    }

    private void setStorageType(StorageType value) {
        privateStorageType = value;
    }

    private String privateStoragePoolName;

    public String getStoragePoolName() {
        return privateStoragePoolName;
    }

    private void setStoragePoolName(String value) {
        privateStoragePoolName = value;
    }

    private Guid privateMasterDomainId = new Guid();

    public Guid getMasterDomainId() {
        return privateMasterDomainId;
    }

    private void setMasterDomainId(Guid value) {
        privateMasterDomainId = value;
    }

    private java.util.ArrayList<Guid> privateDomainsIdList;

    public java.util.ArrayList<Guid> getDomainsIdList() {
        return privateDomainsIdList;
    }

    private void setDomainsIdList(java.util.ArrayList<Guid> value) {
        privateDomainsIdList = value;
    }

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
