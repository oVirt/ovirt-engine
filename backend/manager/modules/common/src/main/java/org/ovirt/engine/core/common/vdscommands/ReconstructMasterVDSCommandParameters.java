package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.compat.Guid;

public class ReconstructMasterVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private int privateVdsSpmId;

    public int getVdsSpmId() {
        return privateVdsSpmId;
    }

    public void setVdsSpmId(int value) {
        privateVdsSpmId = value;
    }

    private Guid privateStoragePoolId = Guid.Empty;

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

    private Guid privateMasterDomainId = Guid.Empty;

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
    }

    @Override
    public String toString() {
        return String.format("%s, vdsSpmId = %d, storagePoolId = %s, "
            + "storagePoolName = %s, masterDomainId = %s, masterVersion = %s, "
            + "domainsList = [%s]",
                super.toString(),
                getVdsSpmId(),
                getStoragePoolId(),
                getStoragePoolName(),
                getMasterDomainId(),
                getMasterVersion(),
                getPrintableDomainsList());
    }

    private String getPrintableDomainsList() {
        StringBuilder sb = new StringBuilder();
        for (StoragePoolIsoMap map : getDomainsList()) {
            sb.append("{ domainId: ");
            sb.append(map.getstorage_id());
            sb.append(", status: ");
            sb.append(map.getstatus().name());
            sb.append(" };");
        }
        return sb.toString();
    }
}
