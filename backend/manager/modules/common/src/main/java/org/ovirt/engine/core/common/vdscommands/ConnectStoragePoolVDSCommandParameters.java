package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ConnectStoragePoolVDSCommandParameters extends VdsIdVDSCommandParametersBase {

    private VDS vds;

    private StoragePool storagePool;

    private Guid masterDomainId;

    private List<StoragePoolIsoMap> storageDomains;

    private boolean refreshOnly;

    public ConnectStoragePoolVDSCommandParameters() {
    }

    public ConnectStoragePoolVDSCommandParameters(VDS vds, StoragePool storagePool, Guid masterDomainId,
                                                  List<StoragePoolIsoMap> storageDomains) {
        this(vds, storagePool, masterDomainId, storageDomains, false);
    }

    public ConnectStoragePoolVDSCommandParameters(VDS vds, StoragePool storagePool, Guid masterDomainId,
                                                  List<StoragePoolIsoMap> storageDomains, boolean refreshOnly) {
        this.vds = vds;
        this.storagePool = storagePool;
        this.masterDomainId = masterDomainId;
        this.storageDomains = storageDomains;
        this.refreshOnly = refreshOnly;
    }

    public VDS getVds() {
        return vds;
    }

    public void setVds(VDS vds) {
        this.vds = vds;
    }

    public Guid getVdsId() {
        return vds.getId();
    }

    public StoragePool getStoragePool() {
        return storagePool;
    }

    public Guid getStoragePoolId() {
        return storagePool.getId();
    }

    public void setStoragePool(StoragePool storagePool) {
        this.storagePool = storagePool;
    }

    public Guid getMasterDomainId() {
        return masterDomainId;
    }

    public List<StoragePoolIsoMap> getStorageDomains() {
        return storageDomains;
    }

    public boolean isRefreshOnly() {
        return this.refreshOnly;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vdsId", vds.getId())
                .append("storagePoolId", storagePool.getId())
                .append("masterVersion", storagePool.getMasterDomainVersion());
    }
}
