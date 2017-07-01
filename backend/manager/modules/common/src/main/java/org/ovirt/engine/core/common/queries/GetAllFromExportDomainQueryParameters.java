package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class GetAllFromExportDomainQueryParameters extends QueryParametersBase {
    private static final long serialVersionUID = 5436719744430725750L;
    private Guid privateStoragePoolId;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    private Guid privateStorageDomainId;

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    private ArrayList<Guid> privateIds;

    public ArrayList<Guid> getIds() {
        return privateIds;
    }

    public void setIds(ArrayList<Guid> value) {
        privateIds = value;
    }

    public GetAllFromExportDomainQueryParameters(Guid storagePoolId, Guid storageDomainId) {
        this.setStoragePoolId(storagePoolId);
        this.setStorageDomainId(storageDomainId);
    }

    public GetAllFromExportDomainQueryParameters() {
        this (Guid.Empty, Guid.Empty);
    }
}
