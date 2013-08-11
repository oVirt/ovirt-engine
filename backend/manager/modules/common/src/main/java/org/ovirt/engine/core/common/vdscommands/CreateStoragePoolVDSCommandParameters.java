package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.compat.Guid;

public class CreateStoragePoolVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid privateStoragePoolId;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    private StorageType privateStorageType;

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

    private Guid privateMasterDomainId;

    public Guid getMasterDomainId() {
        return privateMasterDomainId;
    }

    private void setMasterDomainId(Guid value) {
        privateMasterDomainId = value;
    }

    private List<Guid> privateDomainsIdList;

    public List<Guid> getDomainsIdList() {
        return privateDomainsIdList;
    }

    private void setDomainsIdList(List<Guid> value) {
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
            String poolName, Guid masterDomainId, List<Guid> domainsIdList, int masterVersion) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
        setStorageType(storageType);
        setStoragePoolName(poolName);
        setMasterDomainId(masterDomainId);
        setDomainsIdList(domainsIdList);
        setMasterVersion(masterVersion);
    }

    public CreateStoragePoolVDSCommandParameters() {
        privateStoragePoolId = Guid.Empty;
        privateStorageType = StorageType.UNKNOWN;
        privateMasterDomainId = Guid.Empty;
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
