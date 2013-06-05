package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllFromExportDomainQueryParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 5436719744430725750L;
    private Guid privateStoragePoolId = Guid.Empty;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    private Guid privateStorageDomainId = Guid.Empty;

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    private java.util.ArrayList<Guid> privateIds;

    public java.util.ArrayList<Guid> getIds() {
        return privateIds;
    }

    public void setIds(java.util.ArrayList<Guid> value) {
        privateIds = value;
    }

    public GetAllFromExportDomainQueryParameters(Guid storagePoolId, Guid storageDomainId) {
        this.setStoragePoolId(storagePoolId);
        this.setStorageDomainId(storageDomainId);
    }

    public GetAllFromExportDomainQueryParameters() {
    }
}
