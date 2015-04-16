package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class CreateStoragePoolVDSCommandParameters extends VdsIdVDSCommandParametersBase {
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

    public CreateStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId,
            String poolName, Guid masterDomainId, List<Guid> domainsIdList, int masterVersion) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
        setStoragePoolName(poolName);
        setMasterDomainId(masterDomainId);
        setDomainsIdList(domainsIdList);
        setMasterVersion(masterVersion);
    }

    public CreateStoragePoolVDSCommandParameters() {
        privateStoragePoolId = Guid.Empty;
        privateMasterDomainId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("storagePoolId", getStoragePoolId())
                .append("storagePoolName", getStoragePoolName())
                .append("masterDomainId", getMasterDomainId())
                .append("domainsIdList", getDomainsIdList())
                .append("masterVersion", getMasterVersion());
    }
}
