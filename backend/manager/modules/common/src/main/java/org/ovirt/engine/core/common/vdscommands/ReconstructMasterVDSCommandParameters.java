package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

import java.util.List;

public class ReconstructMasterVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private int privateVdsSpmId;

    public int getVdsSpmId() {
        return privateVdsSpmId;
    }

    public void setVdsSpmId(int value) {
        privateVdsSpmId = value;
    }

    private Guid privateStoragePoolId = new Guid();

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

    private Guid privateMasterDomainId = new Guid();

    public Guid getMasterDomainId() {
        return privateMasterDomainId;
    }

    private void setMasterDomainId(Guid value) {
        privateMasterDomainId = value;
    }

    private List<storage_pool_iso_map> privateDomainsList;

    public List<storage_pool_iso_map> getDomainsList() {
        return privateDomainsList;
    }

    private void setDomainsList(List<storage_pool_iso_map> value) {
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
            String storagePoolName, Guid masterDomainId, List<storage_pool_iso_map> domainsList,
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
        return String.format("%s, vdsSpmId = %i, storagePoolId = %s, "
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
