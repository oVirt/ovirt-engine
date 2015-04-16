package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ReconstructMasterVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private int privateVdsSpmId;

    public int getVdsSpmId() {
        return privateVdsSpmId;
    }

    public void setVdsSpmId(int value) {
        privateVdsSpmId = value;
    }

    private Guid privateStoragePoolId;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
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

    private List<StoragePoolIsoMap> privateDomainsList;

    public List<StoragePoolIsoMap> getDomainsList() {
        return privateDomainsList;
    }

    private void setDomainsList(List<StoragePoolIsoMap> value) {
        privateDomainsList = value;
    }

    private int privateMasterVersion;

    public int getMasterVersion() {
        return privateMasterVersion;
    }

    private void setMasterVersion(int value) {
        privateMasterVersion = value;
    }

    public ReconstructMasterVDSCommandParameters(Guid vdsId, int vdsSpmId, Guid storagePoolId,
            String storagePoolName, Guid masterDomainId, List<StoragePoolIsoMap> domainsList,
            int masterVersion) {
        super(vdsId);
        setVdsSpmId(vdsSpmId);
        setStoragePoolId(storagePoolId);
        setStoragePoolName(storagePoolName);
        setMasterDomainId(masterDomainId);
        setDomainsList(domainsList);
        setMasterVersion(masterVersion);
    }

    public ReconstructMasterVDSCommandParameters() {
        privateStoragePoolId = Guid.Empty;
        privateMasterDomainId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vdsSpmId", getVdsSpmId())
                .append("storagePoolId", getStoragePoolId())
                .append("storagePoolName", getStoragePoolName())
                .append("masterDomainId", getMasterDomainId())
                .append("masterVersion", getMasterVersion())
                .append("domainsList", getDomainsList());
    }
}
